 /*
  * Copyright 2009 Alexis Wilhelm, Romain Tertiaux. This program is free
  * software: you can do anything, but lay off of my blue suede shoes.
  */
 
 package com.izforge.izpack.installer;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Properties;
 import java.util.zip.ZipInputStream;
 import com.izforge.izpack.IPSPack;
 import com.izforge.izpack.LocaleDatabase;
 import com.izforge.izpack.util.AbstractUIProgressHandler;
 import com.sun.pkg.client.ExtendedImage;
 import com.sun.pkg.client.ImagePlanProgressTracker;
 import com.sun.pkg.client.Image.FmriState;
 import com.sun.pkg.client.Image.ImagePlan;
 
 /**
  * An unpacker for the IPS packs.
  * 
  * @author Alexis Wilhelm
  * @author Romain Tertiaux
  * @since January 2009
  */
 public class IPSUnpacker extends UnpackerBase
 {
 	/**
	 * Some callbacks useful for trackje faactions performed
 	 * by {@link ImagePlan#execute(ImagePlanProgressTracker)}.
 	 */
 	private static class ImagePlanProgressTrackerForIzPack extends
 			ImagePlanProgressTracker
 	{
 		/**
 		 * The installation progress handler.
 		 */
 		private final AbstractUIProgressHandler handler;
 
 		/**
 		 * The localized strings.
 		 */
 		private final LocaleDatabase lang;
 
 		/**
 		 * The current step within the current job.
 		 */
 		private int step;
 
 		/**
 		 * Initialize this tracker.
 		 * 
 		 * @param handler The installation progress handler.
 		 * @param lang The localized strings.
 		 * @param step The current step.
 		 */
 		public ImagePlanProgressTrackerForIzPack (
 				AbstractUIProgressHandler handler, LocaleDatabase lang, int step)
 		{
 			this.handler = handler;
 			this.step = step;
 			this.lang = lang;
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void startDownloadPhase (int total)
 		{
 			handler.progress(++step, lang.getString(this, "startDownloadPhase",
 					total, total > 1 ? "s" : ""));
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void startInstallPhase (int total)
 		{
 			handler.progress(++step, lang.getString(this, "startInstallPhase",
 					total, total > 1 ? "s" : ""));
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void startRemovalPhase (int total)
 		{
 			handler.progress(++step, lang.getString(this, "startRemovalPhase",
 					total, total > 1 ? "s" : ""));
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void startUpdatePhase (int total)
 		{
 			handler.progress(++step, lang.getString(this, "startUpdatePhase",
 					total, total > 1 ? "s" : ""));
 		}
 	}
 
 	/**
 	 * The name for the empty IPS image in this installer's resource set.
 	 */
 	public static final String TEMPLATE = "ips/empty.zip";
 
 	/**
 	 * The IPS image dwelling in the installation directory.
 	 */
 	private static ExtendedImage img = null;
 
 	/**
 	 * @return True if there's an image in the installation directory, false
 	 *         it's still to be created.
 	 */
 	public static boolean hasImage ()
 	{
 		return img != null;
 	}
 
 	/**
 	 * The frame hosting the installation progress handler.
 	 */
 	private final InstallerFrame frame;
 
 	/**
 	 * True when only trivial operations where performed, false otherwise. A
 	 * trivial operation isn't that important; therefore it doesn't require the
 	 * user to press the "next" button.
 	 */
 	private boolean trivial = true;
 
 	/**
 	 * @param data The installation data.
 	 * @param handler The installation progress handler.
 	 * @param parent The frame hosting the installation progress handler.
 	 */
 	public IPSUnpacker (AutomatedInstallData data,
 			AbstractUIProgressHandler handler, InstallerFrame parent)
 	{
 		super(data, handler);
 		frame = parent;
 	}
 
 	/**
 	 * Actually install the IPS packs.
 	 * 
 	 * @see UnpackerBase#run()
 	 */
 	@Override
 	public void run ()
 	{
 		handler.startAction("IPSUnpacker", idata.selectedIPSPacks.size() + 3);
 		int job = 0;
 		try
 		{
 			/*
 			 * Create and prepare a new IPS image in the installation directory.
 			 */
 			handler.nextStep(idata.langpack.getString("beginning"), ++job, 1);
 			handler.progress(0, idata.langpack.getString("emptyimage"));
 			loadImage();
 			img.setProxy(idata.proxy);
 			/*
 			 * Remove installed packages not marked as installed.
 			 */
 			handler.nextStep(idata.langpack.getString("update"), ++job, 2);
 			if (!idata.unwantedIPSPackages.isEmpty())
 			{
 				trivial = false;
 				img.uninstallPackages(idata.unwantedIPSPackages);
 			}
 			/*
 			 * Upgrade installed packages marked as upgradable.
 			 */
 			ImagePlan plan = img.makeInstallPlan(idata.selectedIPSPackages);
 			if (plan.getProposedFmris().length > 0)
 			{
 				trivial = false;
 			}
 			plan.execute(new ImagePlanProgressTrackerForIzPack(handler,
 					idata.langpack, 1));
 			/*
 			 * Install each IPS pack.
 			 */
 			for (IPSPack pack: idata.selectedIPSPacks)
 			{
 				trivial = false;
 				handler.nextStep(pack.getName(), ++job, 4);
 				installPack(pack);
 			}
 			idata.selectedIPSPacks.clear();
 			/*
 			 * Install the Sun Update Center if the user wants it to be done.
 			 */
 			if (idata.installUpdateCenter)
 			{
 				handler.nextStep(
 						idata.langpack.getString("IPSInstallPanel.updatecenterinstall"),
 						++job, 1);
 				installUpdateCenter();
 			}
 			/*
 			 * Get the current state of the image.
 			 */
 			loadImage();
 			idata.installedIPSPackages.clear();
 			for (FmriState pkg: img.getInventory(null, true))
 			{
 				if (pkg.installed)
 				{
 					idata.installedIPSPackages.add(pkg);
 				}
 			}
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			handler.emitError(e.getClass().getSimpleName(), e.toString());
 		}
 		finally
 		{
 			handler.stopAction();
 		}
 		/*
 		 * Skip to the next panel if no non trivial operations is performed.
 		 */
 		if (trivial)
 		{
 			frame.skipPanel();
 		}
 	}
 
 	/**
 	 * Install an IPS pack in the local IPS image.
 	 * 
 	 * @param pack The IPS pack we're to install.
 	 */
 	private void installPack (IPSPack pack)
 	{
 		int step = 0;
 		try
 		{
 			/*
 			 * Update an existing authority or add an additional authority for
 			 * packages retrieving.
 			 */
 			handler.progress(
 					++step,
 					idata.langpack.getString("IPSInstallPanel.retrievingcatalog"));
 			img.addAuthority(pack.getAuthority());
 			/*
 			 * Install the packages this pack requires.
 			 */
 			handler.progress(
 					++step,
 					idata.langpack.getString("IPSInstallPanel.resolvingdependencies"));
 			img.makeInstallPlan(pack.getPackages(img.getInventory())).execute(
 					new ImagePlanProgressTrackerForIzPack(handler,
 							idata.langpack, step));
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			handler.emitWarning(e.getClass().getSimpleName(),
 					e.getLocalizedMessage());
 		}
 	}
 
 	/**
 	 * Install the Sun Update Center in the local IPS image.
 	 * 
 	 * @throws InterruptedException When the Update Center failed.
 	 * @throws IOException When we didn't even manage to launch the Update
 	 *         Center.
 	 */
 	private void installUpdateCenter () throws InterruptedException,
 			IOException
 	{
 		/*
 		 * Create the bootstrap configuration file.
 		 */
 		Properties p = new Properties();
 		p.setProperty("image.path", idata.getInstallPath());
 		p.setProperty("install.pkg", "true");
 		p.setProperty("install.updatetool", "true");
 		switch (idata.proxy.type())
 		{
 			case HTTP:
 				p.setProperty("proxy.URL", idata.proxy.address().toString());
 				break;
 			case SOCKS:
 				/*
 				 * TODO What should we do in this case?
 				 */
 				break;
 		}
 		String configFile = idata.getInstallPath()
 				+ "/pkg/lib/bootstrap.properties";
		File dir = new File(idata.getInstallPath() + "/pkg/lib");
		dir.mkdirs();
 		p.store(
 				new PrintWriter(new BufferedWriter(new FileWriter(configFile))),
 				"Parameters for the UC Bootstrap");
 		/*
 		 * Prepare the command line.
 		 */
 		String[] args = { "java", "-jar",
 				idata.getInstallPath() + "/pkg/lib/pkg-bootstrap.jar",
 				configFile };
 		/*
 		 * Launch the bootstrap. The path "/pkg/lib" is there to allow
 		 * `pkg-bootstrap.jar` to find `pkg-client.jar`, which is required.
 		 */
		Process bootstrap = Runtime.getRuntime().exec(args, null, dir);
 		/*
 		 * If the command went fine, launch the Sun Update Center.
 		 */
 		if (bootstrap.waitFor() == 0)
 		{
            // TODO: there is a problem when the install path contains a space
			Runtime.getRuntime().exec(idata.getInstallPath().replaceAll(" ", "\\ ") + "/updatetool/bin/updatetool") ;
 		}
 		else
 		{
 			handler.emitNotification(idata.langpack.getString("IPSInstallPanel.updatecentererror"));
 		}
 	}
 
 	/**
 	 * Read the IPS image dwelling in the installation directory. Create a new
 	 * image if there's none available.
 	 * <p>
 	 * Please note that this method can't override an existing image with a
 	 * fancy IPS not-so-empty image.
 	 * </p>
 	 * 
 	 * @throws Exception When the image can't be created for some reason.
 	 */
 	private void loadImage () throws Exception
 	{
 		File dir = new File(idata.getInstallPath());
 		try
 		{
 			img = new ExtendedImage(dir);
 		}
 		catch (Exception e)
 		{
 			img = ExtendedImage.create(dir, new ZipInputStream(
 					UnpackerBase.class.getResourceAsStream("/res/" + TEMPLATE)));
 		}
 	}
 }
