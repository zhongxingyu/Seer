 package info.sholes.camel.updater;
 
 import java.io.File;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.DialogInterface.OnClickListener;
 import android.content.pm.PackageInfo;
 import android.net.Uri;
 
 public class DownloadHelper {
 	private static XMLElementDecorator xed = null;
 	private static void init(Context ctx) throws Exception {
 		if(xed == null) {
 			String url = ctx.getString(R.string.url_update);
 			try {
 				xed = XMLElementDecorator.parse(url).getChild("smupdater");
 			} catch(Exception e) {
 				throw new Exception("Unable to contact downloads server " + url, e);
 			}
 		}
 	}
 	private static XMLElementDecorator getFileXml(String type) throws Exception {
 		for(XMLElementDecorator file : xed.getChild("files").getChildren("file")) {
 			if(!type.equals(file.getAttribute("type")))
 				continue;
 			return file;
 		}
 		return null;
 	}
 
 	enum Downloadable {
 		ROOT("root"),
 		FLASH_IMAGE("flash_image"),
 		RECOVERY_IMAGE("recovery");
 
 		private final String type;
 		private String url = null;
 		private String md5 = null;
 
 		private XMLElementDecorator xed = null;
 
 		Downloadable(String type) {
 			this.type = type;
 		}
 
 		public String getUrl() throws Exception {
 			if(url != null)
 				return url;
 			if(xed == null)
 				xed = getFileXml(type);
 			url = xed.getChild("url").getString();
 			return url;
 		}
 
 		public String getMd5() throws Exception {
 			if(md5 != null)
 				return md5;
 			if(xed == null)
 				xed = getFileXml(type);
 			md5 = xed.getChild("md5").getString();
 			return md5;
 		}
 
 	}
 
 	enum RomType {
 		ROM_TGZ,
 		UPDATE_ZIP;
 	}
 
 	class RomDescriptor {
 		public final RomType type;
 		public final String name;
 		public final String dispid;
 		public final String url;
 		public final String md5;
 		private RomDescriptor(RomType type, String name, String dispid, String url, String md5) {
 			this.type = type;
 			this.name = name;
 			this.dispid = dispid;
 			this.url = url;
 			this.md5 = md5;
 		}
 	}
 
 	private final Updater u;
 	private final DownloadUtil du;
 	private int download_attempts = 0;
 
 	public DownloadHelper(Updater u) throws Exception {
 		init(u);
 		this.u = u;
 		du = new DownloadUtil(u);
 	}
 
 	public void resetDownloadAttempts() {
 		download_attempts = 0;
 	}
 
 	public File downloadFile(Downloadable which, File where, Callback cb) throws Exception {
 		return downloadFile(which.getUrl(), which.getMd5(), where, cb);
 	}
 
 	private File downloadFile(String url, String expect_md5, File where, Callback cb) throws Exception {
 		while(where.exists()) {
 			String actual_md5;
 			try {
 				u.addText("Calculating md5 of " + where.getName());
 				actual_md5 = DownloadUtil.md5(where);
 			} catch (Exception e) {
 				// Re-download
 				break;
 			}
 			if(expect_md5.equals(actual_md5)) {
 				u.addText("Pass");
 				// Got the file
 				return where;
 			} else {
 				u.addText("Fail: " + actual_md5);
 				// Fall-through to re-download
 				break;
 			}
 		}
 
 		download_attempts++;
 		if(download_attempts >= 3)
			throw new Exception("Failed to download " + url);
 		du.downloadFile(where, new URL(url), cb);
 		return null;
 	}
 
 	public List<RomDescriptor> getRoms() {
 		List<RomDescriptor> roms = new ArrayList<RomDescriptor>();
 
 		XMLElementDecorator e_zips = xed.getChild("zips");
 		if(e_zips != null)
 			for(XMLElementDecorator rom : e_zips.getChildren("zip")) {
 				String name = rom.getAttribute("name");
 				String dispid = rom.getAttribute("dispid");
 				String url = rom.getChild("url").getString();
 				String md5 = rom.getChild("md5").getString();
 				roms.add(new RomDescriptor(RomType.UPDATE_ZIP, name, dispid, url, md5));
 			}
 
 		XMLElementDecorator e_roms = xed.getChild("roms");
 		if(e_roms != null)
 			for(XMLElementDecorator rom : e_roms.getChildren("rom")) {
 				String name = rom.getAttribute("name");
 				String dispid = rom.getAttribute("dispid");
 				String url = rom.getChild("url").getString();
 				String md5 = rom.getChild("md5").getString();
 				roms.add(new RomDescriptor(RomType.ROM_TGZ, name, dispid, url, md5));
 			}
 
 		return roms;
 	}
 
 	public File downloadRom(RomDescriptor rd, File rom_tgz) throws Exception {
 		return downloadFile(rd.url, rd.md5, rom_tgz, Callback.ROM_DOWNLOAD);
 	}
 
 	public boolean checkVersion(PackageInfo pi) {
 		XMLElementDecorator vc = xed.getChild("version_check");
 		int code = vc.getChild("code").getInt().intValue();
 		if(code <= pi.versionCode)
 			return false;
 
 		// Update available!
 		final String name = vc.getChild("name").getString();
 		final String uri = vc.getChild("uri").getString();
 		new AlertDialog.Builder(u)
 		.setTitle(R.string.update_available)
 		.setMessage("Version " + name + " of " + u.getString(R.string.app_label) + " is available")
 		.setPositiveButton(
 				R.string.update_available_pos,
 				new OnClickListener() {
 					public void onClick(DialogInterface dialog, int which) {
 						u.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(uri)));
 						System.exit(1);
 					}
 				}
 		)
 		.setNegativeButton(
 				R.string.update_available_neg,
 				new OnClickListener() {
 					public void onClick(DialogInterface dialog, int which) {
 						System.exit(1);
 					}
 				}
 		)
 		.show();
 
 		return true;
 	}
 
 }
