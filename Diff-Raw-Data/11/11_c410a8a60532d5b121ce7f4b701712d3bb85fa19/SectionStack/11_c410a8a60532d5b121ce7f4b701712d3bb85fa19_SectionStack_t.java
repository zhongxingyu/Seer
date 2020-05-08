 package mustache.core;
 
 import java.util.ArrayDeque;
 import java.util.Deque;
 
 public class SectionStack {
 	
 	private final Deque<Section> sections;
 	
 	public SectionStack(Object root) {
 		this.sections = new ArrayDeque<Section>();
 		this.sections.push( Section.rootSection(root) );
 	}
 	
	private Section findSection(String query, boolean inverted) {
 		for (Section section : sections) {
 			if (section.hasBaseVariable(query)) {
 				return section;
 			}
 		}
		return inverted ? sections.element() : null;
 	}
 	
 	public String getValue(String query) {
		Section section = findSection(query, false);
 		if (section == null) {
 			return "";
 		}
 		Object value = section.getVariable(query);
 		return value == null ? "" : value.toString();
 	}
 
 	private boolean openSection(String query, boolean inverted) {
		Section section = findSection(query, inverted);
 		if (section == null) {
 			return false;
 		}
 		Section newSection = section.open(query, inverted);
 		if (newSection != null) {
 			sections.push(newSection);
 		}
 		return newSection != null;
 	}
 
 	public boolean openSection(String query) {
 		return openSection(query, false);
 	}
 	
 	public boolean openInvertedSection(String query) {
 		return openSection(query, true);
 	}
 	
 	public boolean closeSection(String query) {
 		boolean close = sections.element().close(query);
 		if (close) {
 			sections.pop();
 		}
 		return close;
 	}
 	
 }
