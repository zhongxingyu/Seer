 package dashboard.model;
 
 public enum Course {
 		
 	H01A8A("Algemene en technishce scheikunde",7),
 	H01B0A("Toegepaste Mechanica 1",5),
 	H01B2A("Algemene natuurkunde",7),
 	H01B4A("Thermodynamica",3),
 	H01D0A("Inleiding to de materiaalkunde",3),
 	H01Z2A("Elektrische netwerken",3),
 	H01B6B("Methodiek van de informatica",6),
 	H01A4A("Toegepaste algebra",5),
 	H01A0B("Analyse 1",6),
 	H01A2A("Analyse 2",5),
 	H01B9A("P en O 1",4),
 	H01C2A("P en O 2",3),	
 	H01C4B("Wijsbegeerte",3),
 	H01C6A("Organische scheikunde",3),
 	H01C8A("Toegepaste Mechanica 2",5),
 	H01D2A("Informatie-overdracht en -verwerking",5),
 	H01A6A("Kansrekenen en statistiek",3),
 	H08W0A("Analyse 3",3),
 	H01D8B("Numerieke wiskunde",4),
 	H01D4B("P en O 3",4),
 	H01D7B("Economie",3);
 
 	
	private final String realName;
 	private final int credit;
 		
 	/**
 	 * initiates a course
 	 * @param 	name
 	 * 	the name of the course
 	 * @param 	credit
 	 * 	the credit of the course
 	 * @post	
 	 * 	the name equals the given name
 	 * 	|	new.getName() = name
 	 * @post	
 	 * 	the credit equals the given credit
 	 * 	|	new.getCredit() = credit
 	 */
 	private Course(String name, int credit){
		this.realName = name;
 		this.credit = credit;
 	}
 		
 	/**
 	 * @return
 	 * 	returns the name of the course
 	 * 	|	name
 	 */
 	public String getName() {
		return realName;
 	}
 		
 	/**		 
 	 * @return
 	 * 	returns the credit of the course
 	 * 	| credit
 	 */
 	public int getCredit() {
 		return credit;
 	}
 		
 	/**
 	* @return
 	* 	the hours you should study for that particular course
 	* 	|	getCredit()*28
 	*/
 	public int getHoursNeeded(){
 		return getCredit()*28;
 	}
 }
