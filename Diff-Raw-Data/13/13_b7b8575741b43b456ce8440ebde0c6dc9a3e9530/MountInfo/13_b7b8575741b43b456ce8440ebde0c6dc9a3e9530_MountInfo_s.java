 import java.io.InputStream;
 import java.io.IOException;
 import java.io.File;
 import java.util.regex.Pattern;
 import java.util.regex.Matcher;
 import java.util.ArrayList;
 import javax.swing.filechooser.FileSystemView;
 import java.lang.reflect.Method;
 import java.lang.reflect.InvocationTargetException;
 
 /** Finds local mount points and gives usage information.
 */
 public class MountInfo {
 	/** The path to the mount point */
 	public File	path;
 	/** The total size of the filesystem in bytes (used + available) */
 	public long		size;
 	/** The number of bytes currently used on the filesystem (size - available) */
 	public long		used;
 	/** The number of bytes available on the filesystem (size - used) */
 	public long		available;
 	/** Percent used disk space (100 * used / size) */
 	public int		capacityPercent;
 
 	/** Given a path, returns the mount point.
 		@param path			The path to query. Must be a path to a local filesystem.
 								If it is a path to a networked filesystem, it may return null, or a local filesystem.
 		@return				The mount point info for that path
 		@throws IOException	if there is an io error
 	*/
 	public static MountInfo mount(File path) throws IOException {
 		ArrayList<MountInfo>	mountPoints= mounts();
 		MountInfo				best= null;
 		int						bestUnmatched= 2000000000;
 
 		if(!path.isAbsolute()) {
 			throw new IOException("path must be absolute");
 		}
 		for(MountInfo mount : mountPoints) {
 			int		unmatched= 0;
 			File	pathOnMount= path;
 
 			while( (null != pathOnMount) && !pathOnMount.equals(mount.path) ) {
 				pathOnMount= pathOnMount.getParentFile();
 				++unmatched;
 			}
 			if( (null != pathOnMount)
 					&& pathOnMount.equals(mount.path)
 					&& (unmatched < bestUnmatched) ) {
 				best= mount;
 				bestUnmatched= unmatched;
 			}
 		}
 		return best;
 	}
 	/** Gets all the known local mount points.
 		Calls df -k, and looks for filesystems on devices from /dev.
 		@return				The list of mount points and their space information
 		@throws IOException	If there is a problem getting the mount points
 	*/
 	public static ArrayList<MountInfo> mounts() throws IOException {
 		ArrayList<MountInfo>	results= new ArrayList<MountInfo>();
 
 		if(kIsWindows) {
 			FileSystemView	fsv= FileSystemView.getFileSystemView();
 			for(File root : File.listRoots()) {
 				if(fsv.getSystemTypeDescription(root).equals("Local Disk")) {
					results.add(new MountInfo(root)); // only works on Java 1.6 (6.0) and later
 				}
 			}
 		} else {
 			Matcher						devices= dfLinePattern.matcher(_exec("df","-k"));
 
 			while(devices.find()) {
 				results.add(new MountInfo(new File(devices.group(6)),
 												Long.parseLong(devices.group(2)) * 1024,
 												Long.parseLong(devices.group(3)) * 1024,
 												Long.parseLong(devices.group(4)) * 1024,
 												Integer.parseInt(devices.group(5))));
 			}
 		}
 		return results;
 	}
 	/** We use the native java for Windows, since it works with drive letters but not unix mount points */
 	private static final boolean	kIsWindows= System.getProperties().getProperty("os.name","").indexOf("Windows") >= 0;
 	/** df -k results line pattern.<pre>
 		/dev/...
 			[size in k] [used in k] [available in k] [percent used]% /...
 		</pre>Allows for optional line break between device path and sizes.
 	*/
 	private static Pattern	dfLinePattern= Pattern.compile("^/dev/[^\\r\\n]+([\\r\\n]*\\s+)([0-9]+)\\s+([0-9]+)\\s+([0-9]+)\\s+([0-9]+)%\\s+(/[^\\r\\n]*)$", Pattern.MULTILINE+Pattern.DOTALL);
 	/** Executes a shell command and returned stdout as a string.
 		Starts the process, and reads up to 1k at a time from stdout.
 		@param args			The command (arg[0]) and the arguments to run
 		@return				stdout as a string
 		@throws	IOException	If there is a problem
 	*/
 	private static String _exec(String... args) throws IOException {
 		String		out= "";
 		Process		process= Runtime.getRuntime().exec(args);
 		InputStream	in= process.getInputStream();
 		byte[]		buffer= new byte[1024];
 		int			size;
 
 		do	{
 			size= in.read(buffer);
 			if(size > 0) {
 				out+= new String(buffer, 0, size);
 			}
 		} while(size >= 0);
 		return out;
 	}
 	/** Constructor to improve usability internally.
 		@param p	The mount point path
 		@param s	The size of the filesystem in bytes
 		@param u	The used bytes on the filesystem
 		@param a	The available bytes on the filesystem
 		@param c	The percent capacity used on the filesystem
 	*/
 	private MountInfo(File p, long s, long u, long a, int c) {
 		path= p;
 		size= s;
 		used= u;
 		available= a;
 		capacityPercent= c;
 	}
 	/** Constructs from a mount point path.
 		<b>NOTE</b>: Should only be called on Java 1.6 (6.0) or later
 		To compile and run under 1.5 (5.0), we dynamically look up the methods.
 		@param p			The root path
 		@throws IOException	If we are not Java 1.6 (6.0) or later
 	*/
 	private MountInfo(File p) throws IOException {
 		try	{
 			Class<?>	fileClass= (p).getClass();
 			Method		getFreeSpace= fileClass.getDeclaredMethod("getFreeSpace");
 			Method		getTotalSpace= fileClass.getDeclaredMethod("getTotalSpace");
 
 			path= p;
 			available= ((Long)getFreeSpace.invoke(p)).longValue();
 			size= ((Long)getTotalSpace.invoke(p)).longValue();
 			used= size - available;
			capacityPercent= (int)(100 * used / size);
 		} catch(NoSuchMethodException e1) {
 			throw new IOException(e1.toString());
 		} catch(IllegalAccessException e2) {
 			throw new IOException(e2.toString());
 		} catch(InvocationTargetException e3) {
 			throw new IOException(e3.toString());
 		}
 	}
 	/** Test.
 		Gets the mount points and displays the path, capacity, used and size.
 		@param args	The paths to files to determine what mount point they are on.
 	*/
 	public static void main(String... args) {
 		try	{
 			for(MountInfo i : MountInfo.mounts()) {
 				System.out.println(i.path+" is "+i.capacityPercent+"% full ("+i.used+"/"+i.size+" bytes)");
 			}
 		} catch(IOException exception) {
 			System.err.println(exception);
 		}
 		for(String arg : args) {
 			try	{
 				MountInfo	i= MountInfo.mount(new File(arg));
 
 				if(null != i) {
 					System.out.println(arg+" on "+i.path+" is "+i.capacityPercent+"% full ("+i.used+"/"+i.size+" bytes)");
 				} else {
 					System.out.println(arg+" is not on a local filesystem");
 				}
 			} catch(IOException exception) {
 				System.err.println(arg+": "+exception);
 			}
 		}
 	}
 }
 
 /* df -k on Mac
 Filesystem    1024-blocks      Used Available Capacity  Mounted on
 /dev/disk0s2    488050672 470652880  17141792    97%    /
 devfs                 111       111         0   100%    /dev
 /dev/disk1s1    156249952 112859584  43390368    73%    /Volumes/Storage
 /dev/disk2s2    155914088 103959636  51954452    67%    /Volumes/Backup
 map -hosts              0         0         0   100%    /net
 map auto_home           0         0         0   100%    /home
 map -static             0         0         0   100%    /Network/NirvanaExports
 map -static             0         0         0   100%    /Network/LimboExports
 map -static             0         0         0   100%    /Network/LimboFPGA
 map -static             0         0         0   100%    /Network/LimboHelp
 */
 
 /* df -k on Linux
 Filesystem           1K-blocks      Used Available Use% Mounted on
 /dev/mapper/VolGroup00-LogVol00
                      151560372  87131692  56729840  61% /
 /dev/mapper/VolGroup00-LogVol02
                      240419328 127000380 101206316  56% /builds
 /dev/sda1               101086     12426     83441  13% /boot
 none                   1036792         0   1036792   0% /dev/shm
 limbo:/Exports               -         -         -   -  /mnt/limbo
 limbo:/Installers    1757671424 1241470624 516200800  71% /mnt/installers
 limbo:/Exports/labview/help
                              -         -         -   -  /mnt/limboHelp
 limbo:/tools         1757671424 1241470624 516200800  71% /mnt/limboTools
 limbo:/Saturn                -         -         -   -  /mnt/release/Saturn
 limbo:/Orion         1757671424 1241470624 516200800  71% /mnt/release/Orion
 nirvana:/vol/p4exports/perforceExports
                              -         -         -   -  /mnt/nirvana
 lvbuild:/var/www/lvbuild/public/builds/linux
                      113113792  55901408  51466528  53% /mnt/wwwlvbuild
 */
