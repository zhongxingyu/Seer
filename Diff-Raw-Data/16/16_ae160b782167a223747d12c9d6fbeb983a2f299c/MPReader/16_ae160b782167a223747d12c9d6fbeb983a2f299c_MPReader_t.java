 package ru.osm.dkiselev.mpreader;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 
 import ru.osm.dkiselev.mpreader.sections.Section;
 import ru.osm.dkiselev.mpreader.sections.SectionReader;
 import ru.osm.dkiselev.mpreader.sections.SectionsReaderFactory;
 
 public class MPReader {
 	
 	private InputStream is;
 	private MPReadHandler handler;
 	private BufferedReader reader; 
 	
 	private List<String> comments = new ArrayList<String>(0);
	private SectionReader<Section> currentSectionReader;
 	private Section currentSection;
 	
 	private SectionsReaderFactory readersFactory = SectionsReaderFactory.get();
 	
 	public MPReader(InputStream is) {
 		this.is = is;
 	}
 	
 	public void read(MPReadHandler handler) throws IOException {
 		
 		this.handler = handler;
 		
 		reader = new BufferedReader(new InputStreamReader(is));
 		
 		String line = null;
 		
 		do {
 			line = reader.readLine();
 			processLine(line);
 		}
 		while(line != null);
 	}
 
	@SuppressWarnings("unchecked")
 	private void processLine(String line) {
 
 		if(line == null) {
 			return;
 		}
 		
 		line = line.trim();
 		
 		if(line.isEmpty()) {
 			//skip empty lines 
 			return;
 		}
 		
 		//Comments, before or inside section
 		if(line.charAt(0) == ';') {
 			comments.add(line);
 		}
 		//Section start or end
		else if(line.charAt(0) == '[' && line.charAt(line.length() - 1) == ']') {
 			String sectionName = line.substring(1, line.length() - 1);
 			
 			if(sectionName.startsWith("END")) {
 
 				currentSectionReader.setComments(comments, currentSection);
 				currentSectionReader.endSection(currentSection);
 
 				this.comments = new ArrayList<String>(0);
 				
 				handleSection(currentSection);
 				currentSection = null;
 				
 			} else {
 				
 				currentSectionReader = readersFactory.getReaderForSection(sectionName);
 				
 				currentSection = currentSectionReader.newSection();
 			}
 		}
 		// regular string
 		else {
 			currentSectionReader.readLine(line, currentSection);
 		}
 
 	}
 
 	private void handleSection(Section section) {
 		this.handler.handleSection(section);
 	}
 }
