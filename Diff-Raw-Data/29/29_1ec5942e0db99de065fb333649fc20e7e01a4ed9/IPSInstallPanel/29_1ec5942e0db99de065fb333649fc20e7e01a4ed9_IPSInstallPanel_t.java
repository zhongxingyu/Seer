 /*
  * Copyright 2009 Alexis Wilhelm. This program is free software: you can do
  * anything, but lay off of my blue suede shoes.
  */
 
 package com.izforge.izpack.panels;
 
 import javax.swing.SwingUtilities;
 import com.izforge.izpack.installer.IPSUnpacker;
 import com.izforge.izpack.installer.InstallData;
 import com.izforge.izpack.installer.InstallerFrame;
 
 /**
  * A panel that installs the IPS packages. It looks like a standard InstallPanel
  * with a neat detailed view of what happens.
  * <p>
  * Don't forget to put at least one IPSPacksPanel or one IPSUpdatePanel before
  * this panel if you want it to actually do something. Nonetheless putting this
  * panel first can prove useful in order to perform some initializations of the
  * image.
  * </p>
  * 
  * @author Alexis Wilhelm
  * @since January 2009
  * @see IPSPacksPanel
  * @see IPSUpdatePanel
  */
 public class IPSInstallPanel extends InstallPanel
 {
 	/**
 	 * Allow this panel to get serialized.
 	 */
 	private static final long serialVersionUID = 2465668656165262925L;
 
 	/**
 	 * Construct this panel.
 	 * 
 	 * @param parent The frame hosting this panel.
 	 * @param data The all important object describing everything we need for
 	 *        the installation.
 	 */
 	public IPSInstallPanel (InstallerFrame parent, InstallData data)
 	{
 		super(parent, data);
 	}
 
 	/**
 	 * Install each selected IPS package, or skip to the next panel if no IPS
 	 * pack/package is selected. The unpacker is allowed to skip to the next
 	 * panel once it's done in case it only performed trivial operations. This
 	 * method gets called when this panel becomes active.
 	 * 
 	 * @see InstallPanel#panelActivate()
 	 */
 	@Override
 	public void panelActivate ()
 	{
 		parent.lockNextButton();
 		if (idata.selectedIPSPacks.isEmpty()
 				&& idata.selectedIPSPackages.isEmpty()
 				&& idata.unwantedIPSPackages.isEmpty()
 				&& IPSUnpacker.hasImage())
 		{
 			parent.skipPanel();
 		}
 		else
 		{
			//SwingUtilities.invokeLater(new IPSUnpacker(idata, this, parent));
            new Thread(new IPSUnpacker(idata, this, parent)).start();
 		}
 	}
 }
