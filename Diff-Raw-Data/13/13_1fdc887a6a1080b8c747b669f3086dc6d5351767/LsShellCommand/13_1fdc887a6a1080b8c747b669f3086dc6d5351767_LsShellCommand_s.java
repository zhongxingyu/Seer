 package hr.fer.zemris.java.tecaj.hw5;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.IOException;
 import java.nio.file.FileVisitResult;
 import java.nio.file.FileVisitor;
 import java.nio.file.Files;
 import java.nio.file.LinkOption;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.nio.file.attribute.BasicFileAttributeView;
 import java.nio.file.attribute.BasicFileAttributes;
 import java.nio.file.attribute.FileTime;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 public class LsShellCommand implements ShellCommand {
 
 	@Override
 	public ShellStatus executeCommand(BufferedReader in, BufferedWriter out, String[] arguments) {
 		arguments = ShellUtils.checkIfMultiLine(in, arguments);
 		
 		if(arguments.length != 1) {
 			return ShellUtils.error(out, "'ls' command accepts one arguments - directory you want to list.");
 		}
 		
 		Path directory = Paths.get(arguments[0]);
 		if(!directory.toFile().isDirectory()) {
 			return ShellUtils.error(out, "'" + arguments[0] + "' is not a directory.");
 		}
 		
 		try {
			DirectoryVisitor radnik = new DirectoryVisitor();
			Files.walkFileTree(directory, radnik);
 		} catch (IOException e) {
			
 		}
 		
 		return ShellStatus.CONTINUE;
 	}
 	
 	class DirectoryVisitor implements FileVisitor<Path> {
 		
 		int depth;
 		
 		@Override
 		public FileVisitResult postVisitDirectory(Path dir, IOException e) throws IOException {
 			depth--;
 			
 			return FileVisitResult.CONTINUE;
 		}
 
 		@Override
 		public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attra) throws IOException {
 			String directoryName = dir.toFile().getName();
 			long directorySize = getDirectorySize(dir.toFile());
 			
 			if(depth > 0) printLine(getPermissions(dir), directorySize, getFormattedDate(dir), directoryName);
 
 			depth++;
 			
 			if(depth > 1)
 				return FileVisitResult.SKIP_SUBTREE;
 			else
 				return FileVisitResult.CONTINUE;
 		}
 		
 		@Override
 		public FileVisitResult visitFile(Path file, BasicFileAttributes arg1) throws IOException {
 			String fileName = file.toFile().getName();
 			long fileSize = Files.size(file);
 			
 			printLine(getPermissions(file), fileSize, getFormattedDate(file), fileName);
 			
 			return FileVisitResult.CONTINUE;
 		}
 
 		@Override
 		public FileVisitResult visitFileFailed(Path arg0, IOException e) throws IOException {
 			return FileVisitResult.CONTINUE;
 		}
 
 		private long getDirectorySize(File dir) {
 			long length = 0;
 		    for (File file : dir.listFiles()) {
 		        if (file.isFile())
 		            length += file.length();
 		        else
 		            length += getDirectorySize(file);
 		    }
 		    return length;
 		}
 		
 		private String getPermissions(Path path) {
 			char directory = '-';
 			if(Files.isDirectory(path)) directory = 'd';
 			
 			char readable = '-';
 			if(Files.isReadable(path)) readable = 'r';
 			
 			char writeable = '-';
 			if(Files.isWritable(path)) writeable = 'w';
 			
 			char executable = '-';
 			if(Files.isExecutable(path)) executable = 'x';
 			
 			return new StringBuilder().append(directory).append(readable).append(writeable).append(executable).toString();
 		}
 		
 		private void printLine(String permissions, long fileSize, String formattedDateTime, String fileName) {
 			System.out.println(String.format("%s %10d %s %s", permissions, fileSize, formattedDateTime, fileName));
 		}
 		
 		private String getFormattedDate(Path path) throws IOException {
 			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 			
 			BasicFileAttributeView faView = Files.getFileAttributeView(path,
 					BasicFileAttributeView.class, LinkOption.NOFOLLOW_LINKS);
 			BasicFileAttributes attributes = faView.readAttributes();
 			FileTime fileTime = attributes.creationTime();
 			return sdf.format(new Date(fileTime.toMillis()));
 		}
 	}
 }
