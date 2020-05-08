 package com.mamykin.psytest.client.model;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class Study {
 	private String name = "";
 	private List<StudyGroup> groups = new ArrayList<StudyGroup>();
 	private List<StudyBlock> blocks = new ArrayList<StudyBlock>();
 	private List<StudySlide> slideTemplates = new ArrayList<StudySlide>();
 	private int caseCount;
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public void setCaseCount(int value) {
 		caseCount = value;
 	}
 
 	public int getCaseCount() {
 		return caseCount;
 	}
 
 	public List<StudyGroup> getGroups() {
 		return groups;
 	}
 
 	public void addGroup(StudyGroup group) {
 		this.groups.add(group);
 	}
 
 	public List<StudySlide> getSlideTemplates() {
 		return slideTemplates;
 	}
 
 	public void addSlideTemplate(StudySlide slide) {
 		slideTemplates.add(slide);
 	}
 
 	public void addBlock(StudyBlock parseBlock) {
 		this.blocks.add(parseBlock);
 	}
 
 	public StudyInfo getStudyInfo() {
 		return new StudyInfo(getName(), getGroupNames(), getCaseNames());
 	}
 
 	private List<String> getCaseNames() {
 		List<String> caseNames = new ArrayList<String>();
 		for (int i = 1; i <= getCaseCount(); i++) {
 			caseNames.add(Integer.toString(i));
 		}
 		return caseNames;
 	}
 
 	private List<String> getGroupNames() {
 		List<String> groupNames = new ArrayList<String>();
 		for (StudyGroup group : groups) {
 			groupNames.add(group.getName());
 		}
 		return groupNames;
 	}
 
 	public StudyRun createRun(String groupName, String caseName, String participant) {
 		StudyGroup group = lookupGroup(groupName);
 		int startingCase = Integer.parseInt(caseName);
 		return new StudyRun(participant, groupName, caseName, calculateRunSlides(group, startingCase));
 	}
 
 	private List<StudySlide> calculateRunSlides(StudyGroup group, int startingCase) {
 		List<StudySlide> runSlides = new ArrayList<StudySlide>();
 		// find all blocks for the group references
 		for (String blockRef : group.getBlockRefs()) {
 			StudyBlock block = lookupBlock(blockRef);
 			for (int i = 0; i < block.getCases().size(); i++) {
 				// arrange the cases in the order starting with startingcase
				int actualIndex = getActualCaseIndex(i, startingCase, block.getCases().size());
				StudyCase currentCase = block.getCases().get(actualIndex);
				for (StudySlide slide : currentCase.getSlides()) {
 					String templateName = slide.getTemplate();
 					StudySlide completeSlide;
 					if (null != templateName) {
 						StudySlide template = lookupSlideTemplate(templateName);
 						completeSlide = slide.buildFrom(template);
 					} else {
 						completeSlide = slide;
 					}
 					completeSlide.setBlock(blockRef);
 					completeSlide.setCaseIndex(Integer.toString(actualIndex));
 					runSlides.add(completeSlide);
 				}
 			}
 		}
 		return runSlides;
 	}
 
	private int getActualCaseIndex(int currentIndex, int startingIndex, int casesInBlock) {
		int actualIndex = startingIndex + currentIndex;
		while (actualIndex >= casesInBlock) {
			actualIndex -= casesInBlock;
		}
		return actualIndex;
	}

 	private StudyGroup lookupGroup(String groupName) {
 		for (StudyGroup group : groups) {
 			if (group.getName().equals(groupName)) {
 				return group;
 			}
 		}
 		return null;
 	}
 
 	private StudyBlock lookupBlock(String blockRef) {
 		for (StudyBlock block : blocks) {
 			if (block.getId().equals(blockRef)) {
 				return block;
 			}
 		}
 		throw new IllegalArgumentException("Study xml file does not contain block with name " + blockRef);
 	}
 
 	private StudySlide lookupSlideTemplate(String templateName) {
 		for (StudySlide slide : slideTemplates) {
 			if (slide.getName().equals(templateName)) {
 				return slide;
 			}
 		}
 		throw new IllegalArgumentException("Study xml file does not contain slide template with name " + templateName);
 	}
 }
