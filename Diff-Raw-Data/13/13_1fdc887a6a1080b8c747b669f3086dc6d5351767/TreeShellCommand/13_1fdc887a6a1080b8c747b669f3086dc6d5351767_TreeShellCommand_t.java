 package hr.fer.zemris.java.tecaj.hw5;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.nio.file.FileVisitResult;
 import java.nio.file.FileVisitor;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.nio.file.attribute.BasicFileAttributes;
 
 public class TreeShellCommand implements ShellCommand {
 
 	@Override
 	public ShellStatus executeCommand(BufferedReader in, BufferedWriter out, String[] arguments) {
 		arguments = ShellUtils.checkIfMultiLine(in, arguments);
 		
 		if(arguments.length != 1) {
 			return ShellUtils.error(out, "'tree' command accepts one arguments - directory you want to list.");
 		}
 		
 		Path directory = Paths.get(arguments[0]);
 		if(!directory.toFile().isDirectory()) {
 			return ShellUtils.error(out, "'" + arguments[0] + "' is not a directory.");
 		}
 		
 		try {
 			Files.walkFileTree(directory, new PrintTree());
 		} catch (IOException e) {
			return ShellUtils.error(out, "Error with IO operation.");
 		}
 		
 		return ShellStatus.CONTINUE;
 	}
 	
 	class PrintTree implements FileVisitor<Path> {
 		
 		int depth;
 		
 		@Override
 		public FileVisitResult postVisitDirectory(Path dir, IOException e) throws IOException {
 			depth--;
 			
 			return FileVisitResult.CONTINUE;
 		}
 
 		@Override
 		public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attra) throws IOException {
 			if(depth == 0) {
 				System.out.println(dir.toFile().getAbsolutePath());
 			} else {
 				System.out.println(String.format("%" + (depth*2) + "s", "") + dir.toFile().getName());
 			}
 			
 			depth++;
 			
 			return FileVisitResult.CONTINUE;
 		}
 		
 		@Override
 		public FileVisitResult visitFile(Path file, BasicFileAttributes arg1) throws IOException {
 			String fileName = file.toFile().getName();
 			System.out.println(String.format("%" + (depth*2) + "s", "") + fileName);
 			
 			return FileVisitResult.CONTINUE;
 		}
 
 		@Override
 		public FileVisitResult visitFileFailed(Path arg0, IOException e) throws IOException {
 			return FileVisitResult.CONTINUE;
 		}
 	}
 }
