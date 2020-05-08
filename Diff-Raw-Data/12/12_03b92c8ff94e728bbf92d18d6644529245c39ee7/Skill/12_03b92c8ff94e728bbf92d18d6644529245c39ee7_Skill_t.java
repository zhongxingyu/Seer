 package org.yarquen.account;
 
 import javax.validation.constraints.NotNull;
 
 import org.yarquen.category.CategoryBranch;
 import org.yarquen.category.CategoryBranchNode;
 
 public class Skill {
 	public static enum Level {
 		UNKNOW(0, "Unknow"), BASIC(1, "Basic"), MEDIUM(2, "Medium"), ADVANCED(
 				3, "Advanced");
 		private final int id;
 		private final String name;
 
 		Level(int id, String name) {
 			this.id = id;
 			this.name = name;
 		}
 
 		public int getId() {
 			return id;
 		}
 
 		public String getName() {
 			return name;
 		}
 
 		public static Level parse(int value) {
 			for (Level l : Level.values()) {
 				if (value == l.getId()) {
 					return l;
 				}
 			}
 			throw new IllegalArgumentException("Invalid level value(" + value
 					+ "), it has to be in the range [0, 3]");
 		}
 	}
 
 	public static final String LEVEL_SEPARATOR = ".";
 
 	@NotNull
 	private CategoryBranch categoryBranch;
 	private int level;
 
 	public int getLevel() {
 		return level;
 	}
 
 	public void setLevel(int level) {
 		this.level = level;
 	}
 
 	public String getAsText() {
 		return categoryBranch.getCode() + LEVEL_SEPARATOR + level;
 	}
 
 	public CategoryBranch getCategoryBranch() {
 		return categoryBranch;
 	}
 
 	public String getCode() {
 		String code = categoryBranch.getCode();
 		if (level != Level.UNKNOW.getId()) {
 			code += LEVEL_SEPARATOR + level;
 		}
 		return code;
 	}
 
 	public String[] getCodeAsArray(String categoryCode) {
 		int arrayLength = 1 + categoryBranch.getNodes().size();
 		if (level != Level.UNKNOW.getId()) {
 			arrayLength++;
 		}
 		final String[] codeArray = new String[arrayLength];
 		int i = 0;
 		codeArray[i++] = categoryCode;
 		for (CategoryBranchNode node : categoryBranch.getNodes()) {
 			codeArray[i++] = node.getCode();
 		}
 		if (level != Level.UNKNOW.getId()) {
 			codeArray[i++] = String.valueOf(level);
 		}
 		return codeArray;
 	}
 
 	public String getLevelName() {
 		return Level.parse(level).getName();
 	}
 
 	public void setCategoryBranch(CategoryBranch categoryBranch) {
 		this.categoryBranch = categoryBranch;
 	}
 
 	@Override
 	public String toString() {
 		return "AccountSkill [categoryBranch=" + categoryBranch + ", level="
 				+ level + "]";
 	}

 	@Override
 	public boolean equals(Object anObject) {
 		if (this == anObject) {
 			return true;
 		}
 
 		if (anObject instanceof Skill) {
 			final Skill skill = (Skill) anObject;
 			if (this.getCode().equals(skill.getCode())) {
 				return true;
 			}
 		}
 		return false;
 
 	}

	public String friendlyName() {
		return categoryBranch.getNodes()
				.get(categoryBranch.getNodes().size() - 1).getName()
				+ ": " + getLevelName();
	}

 }
