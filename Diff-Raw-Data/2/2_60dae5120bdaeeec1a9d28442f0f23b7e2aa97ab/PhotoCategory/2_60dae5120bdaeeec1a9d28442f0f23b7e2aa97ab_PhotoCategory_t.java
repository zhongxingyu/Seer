 /**
  * 
  */
 package com.github.yuyang226.j500px.photos;
 
 /**
  * @author yayu
  * @see http://developer.500px.com/docs/formats#site_feature
  */
 public enum PhotoCategory {
 
 	Uncategorized(0),
 	Abstract(10),
 	Animals(11),
 	BlackAndWhite(5, "Black and White"),
 	Celebrities(1),
 	CityAndArchitecture(9, "City and Architecture"),
 	Commercial(15),
 	Concert(16),
 	Family(20),
 	Fashion(14),
 	Film(2),
	FineArt(24, "Fine Art"),
 	Food(23),
 	Journalism(3),
 	Landscapes(8),
 	Macro(12),
 	Nature(18),
 	Nude(4),
 	People(7),
 	PerformingArts(19, "Performing Arts"),
 	Sport(17),
 	StillLife(6, "Still Life"),
 	Street(21),
 	Transporation(26),
 	Travel(13),
 	Underwater(22),
 	UrbanExploration(27, "Urban Exploration"),
 	Wedding(25);
 	
 	private int categoryId;
 	private String categoryName;
 	
 	private PhotoCategory(int categoryId) {
 		this.categoryId = categoryId;
 	}
 	
 	private PhotoCategory(int categoryId, String categoryName) {
 		this.categoryId = categoryId;
 		this.categoryName = categoryName;
 	}
 	
 	/**
 	 * @return the categoryId
 	 */
 	public int getCategoryId() {
 		return categoryId;
 	}
 	
 	/**
 	 * @return the categoryName
 	 */
 	public String getCategoryName() {
 		return categoryName == null ? toString() : categoryName;
 	}
 
 	public static PhotoCategory valueOf(int value) {
 		for (PhotoCategory category: PhotoCategory.values()) {
 			if (category.getCategoryId() == value) {
 				return category;
 			}
 		}
 		return Uncategorized;
 	}
 	
 	/*18	Nature,
 	4	Nude,
 	7	People,
 	19	PerformingArts,
 	17	Sport,
 	6	StillLife,
 	21	Street,
 	26	Transporation,
 	13	Travel,
 	22	Underwater,
 	27	UrbanExploration,
 	25	Wedding,*/
 }
