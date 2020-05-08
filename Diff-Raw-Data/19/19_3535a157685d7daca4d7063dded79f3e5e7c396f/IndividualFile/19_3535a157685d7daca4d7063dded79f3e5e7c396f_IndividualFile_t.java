 package za.ac.sun.cs.intlola.file;
 
 import java.io.File;
 import java.util.Calendar;
 
 import com.google.gson.JsonObject;
 
 public class IndividualFile implements IntlolaFile {
 	private static final String INTLOLA_SEP = "_";
 	private static final String NAME_SEP = ".";
 
 	public static String getPackage(final String[] holder, final int len) {
 		String pkg = "";
 		boolean start = false;
 		for (int i = 0; i < len; i++) {
 			if (start) {
 				pkg += holder[i];
				if (i < len - 1) {
 					pkg += NAME_SEP;
 				}
 			}
			if (holder[i].equals("src") || holder[i].equals("bin")) {
				start = true;
			}
 		}
 		return pkg;
 	}
 
 	public static IntlolaFile read(final String path) {
 		String[] elems = path.split(File.separator);
 		final String fdata = elems[elems.length - 1];
 		elems = fdata.split(INTLOLA_SEP);
 		final char mod = elems[elems.length - 1].charAt(0);
 		final int num = Integer.parseInt(elems[elems.length - 2]);
 		final long time = Long.parseLong(elems[elems.length - 3]);
 		String name = null, pkg = null;
 		if (elems.length > 3) {
 			name = elems[elems.length - 4];
 			pkg = IndividualFile.getPackage(elems, elems.length - 4);
 		}
 		return new IndividualFile(path, name, pkg, mod, num, time);
 	}
 
 	private final String path, name, pkg;
 	private final char mod;
 	private final int num;
 
 	private final long time;
 
 	private final boolean hasContents;
 
 	public IndividualFile(final String path, final char mod, final int num,
 			final boolean hasContents) {
 		this.path = path;
 		this.mod = mod;
 		this.num = num;
 		this.hasContents = hasContents;
 		time = Calendar.getInstance().getTimeInMillis();
 		final String[] spd = path.split(File.separator);
 		name = spd[spd.length - 1];
 		pkg = getPackage(spd, spd.length - 1);
 	}
 
 	public IndividualFile(final String path, final String name,
 			final String pkg, final char mod, final int num, final long time) {
 		this.path = path;
 		this.mod = mod;
 		this.num = num;
 		hasContents = true;
 		this.time = time;
 		this.name = name;
 		this.pkg = pkg;
 	}
 
 	@Override
 	public String getPath() {
 		return path;
 	}
 
 	@Override
 	public boolean hasContents() {
 		return hasContents;
 	}
 
 	@Override
 	public JsonObject toJSON() {
 		final JsonObject ret = new JsonObject();
 		ret.addProperty(Args.NAME, name);
 		if (hasContents()) {
 			String ext = name.split("\\.")[1];
 			ret.addProperty(Args.FTYPE, ext);
 			if (ext.equals(Args.JAVA)) {
 				ret.addProperty(Args.TYPE, Args.SRC);
 			} else if (ext.equals(Args.CLASS)) {
 				ret.addProperty(Args.TYPE, Args.EXEC);
 			}
 		} else {
 			ret.addProperty(Args.FTYPE, Args.EMPTY);
 			ret.addProperty(Args.TYPE, Args.CHANGE);
 		}
 		ret.addProperty(Args.PKG, pkg);
 		ret.addProperty(Args.MOD, mod);
 		ret.addProperty(Args.NUM, num);
 		ret.addProperty(Args.TIME, time);
 		return ret;
 	}
 }
