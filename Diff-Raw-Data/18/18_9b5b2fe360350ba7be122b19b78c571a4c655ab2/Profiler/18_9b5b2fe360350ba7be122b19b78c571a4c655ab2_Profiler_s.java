 package me.heldplayer.HeldGeneration.profiler;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 public class Profiler {
 	private static Section currentSection = new RootSection();
 
 	static {
 		System.out.println("Profiler for HeldGeneration activated");
 	}
 
 	public static void startSection(String name) {
 		Section section = null;
 
 		if (currentSection.childSections.containsKey(name)) {
 			section = currentSection.childSections.get(name);
 		} else {
 			section = new Section(name);
 
 			section.parent = currentSection;
 		}
 
 		section.calls++;
 
 		section.startNanos = System.nanoTime();
 
 		currentSection.childSections.put(name, section);
 
 		currentSection = section;
 	}
 
 	public static void endSection() {
 		if (!(currentSection instanceof RootSection)) {
			currentSection = currentSection.parent;
 
			currentSection.totalNanos = +System.nanoTime() - currentSection.startNanos;
 		}
 	}
 
 	public static void endAll() {
 		while (!(currentSection instanceof RootSection)) {
 			endSection();
 		}
 	}
 
 	public static void endStartSection(String name) {
 		endSection();
 		startSection(name);
 	}
 
 	public static void saveResults(File location) {
 		if (!location.exists()) {
 			try {
 				location.createNewFile();
 			} catch (IOException e) {
 				e.printStackTrace();
 				return;
 			}
 		}
 		try {
 			FileOutputStream FoS = new FileOutputStream(location);
 
 			writeSection(currentSection, FoS, "");
 
 			FoS.close();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private static void writeSection(Section section, FileOutputStream stream, String prefix) throws IOException {
 		if (!(section instanceof RootSection)) {
 			StringBuilder builder = new StringBuilder();
 
			builder.append(prefix).append(".").append(section.name).append(": ");
 
			builder.append(section.totalNanos / section.calls).append(" nanos, ");
 
			builder.append(section.calls).append(" calls");
 
 			builder.append(System.getProperty("line.separator"));
 
 			stream.write(builder.toString().getBytes());
 
 			for (Section subSection : section.childSections.values()) {
 				writeSection(subSection, stream, prefix + "." + section.name);
 			}
 		} else {
 			for (Section subSection : section.childSections.values()) {
 				writeSection(subSection, stream, prefix + section.name);
 			}
 		}
 
 	}
 }
