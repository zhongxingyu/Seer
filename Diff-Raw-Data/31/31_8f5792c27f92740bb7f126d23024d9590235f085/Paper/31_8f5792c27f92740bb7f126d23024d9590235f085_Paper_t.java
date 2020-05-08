 public abstract class Paper implements Comparable<Paper>/*, Comparator<Paper>*/
 {	
 	/* A variable to store the name of the paper.*/
 	private String name;
 	/* A variable to store the authors of the paper.*/
 	private String authors;
 	/* A variable to store the title of the paper.*/
 	private String title;
 	/* A variable to store the serial title of the paper.*/
 	private String serialTitle;
 	/* A variable to store the date of publication of the paper.*/
 	private String date;
 	/* A variable to store the pages the paper is on.*/
 	private String pageRange;
 	/* A variable to store the digital object identifier of the paper.*/
 	private String DOI;
 	
 	/* A variable to store the method of sorting and searching. */
 	private static String sortCriteria;
 	
 	/**
 	 * Default constructor.
 	 */
 	public Paper() {}
 	
 	/**
 	 * Compares one paper to another by the given criteria.
 	 * @param otherPaper The paper you want to compare against.
 	 */
 	public int compareTo(Paper otherPaper)
 	{
 		int offset = 0;
 		
 		switch (sortCriteria)
 		{
 		case "BI":
 			sortCriteria = "AN";
 			compareTo(otherPaper);
 			break;
 		case "AN":
 			offset = compare(otherPaper, 1);
 			break;
 		case "PT":
 			offset = compare(otherPaper, 2);
 			break;
 		case "ST":
 			offset = compare(otherPaper, 3);
 			break;
 		case "CH":
 			String thisDate = this.toString().split(" // ")[5];
 			String otherDate = otherPaper.toString().split(" // ")[5];
 			//This ugly (but code-lines efficient) replace line turns the date in "MMM YYYY" format to "YYYYMM".  Ex. "May 2011 Becomes 201105.  This becomes sortable by year then month.
 			thisDate = thisDate.split(" ")[1] + " " + thisDate.split(" ")[0].replace("January", "01").replace("February", "02").replace("March", "03").replace("April", "04").replace("May", "05").replace("June","06").replace("July", "07").replace("August", "08").replace("September", "09").replace("October", "10").replace("November", "11").replace("December", "12");
 			otherDate = otherDate.split(" ")[1] + " " + otherDate.split(" ")[0].replace("January", "01").replace("February", "02").replace("March", "03").replace("April", "04").replace("May", "05").replace("June","06").replace("July", "07").replace("August", "08").replace("September", "09").replace("October", "10").replace("November", "11").replace("December", "12");
 			offset = thisDate.compareToIgnoreCase(otherDate);
 			break;
 		default:
 			System.out.println("Please specify a searchable criteria.");
 			break;
 		}
 		return offset;
 	}
 	
 	/**
 	 * Used by the compareTo method to simplify comparing papers
 	 * @param otherPaper The paper you want to compare to.
 	 * @param index ************************
 	 * @return
 	 */
 	public int compare(Paper otherPaper, int index)
 	{
 		String otherPaperString = otherPaper.toString();
 		String thisPaperString = this.toString();
 		return thisPaperString.split(" // ")[index].compareToIgnoreCase(otherPaperString.split(" // ")[index]);
 	}
 	
 	/**
 	 * Sets the search criteria
 	 * @param method The search criteria ("BI", "AN", "PT", "ST", "CH", "R")
 	 */
 	public static void setSortCriteria(String method)
 	{
 		sortCriteria = method;
 	}
 	
 	/**
 	 * @return the name of the paper
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * @param the name of the paper
 	 */
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	/**
 	 * @return the authors of the paper
 	 */
 	public String getAuthors() {
 		return authors;
 	}
 
 	/**
 	 * @param the authors of the paper
 	 */
 	public void setAuthors(String authors) {
 		this.authors = authors;
 	}
 
 	/**
 	 * @return the title of the paper
 	 */
 	public String getTitle() {
 		return title;
 	}
 
 	/**
 	 * @param the title of the paper
 	 */
 	public void setTitle(String title) {
 		this.title = title;
 	}
 
 	/**
 	 * @return the Serial title of the paper
 	 */
 	public String getSerialTitle() {
 		return serialTitle;
 	}
 
 	/**
 	 * @param the serial title of the paper
 	 */
 	public void setSerialTitle(String serialTitle) {
 		this.serialTitle = serialTitle;
 	}
 
 	/**
 	 * @return the date of publication
 	 */
 	public String getDate() {
 		return date;
 	}
 
 	/**
 	 * @param the date of publication
 	 */
 	public void setDate(String date) {
 		this.date = date;
 	}
 
 	/**
 	 * @return the pages the paper is found on in the format "start-end"
 	 */
 	public String getPageRange() {
 		return pageRange;
 	}
 
 	/**
 	 * @param the pages the paper is found on in the format "start-end"
 	 */
 	public void setPageRange(String pageRange) {
 		this.pageRange = pageRange;
 	}
 
 	/**
 	 * @return the digital object identifier of the paper
 	 */
 	public String getDOI() {
 		return DOI;
 	}
 
 	/**
 	 * @param the digital object identifier of the paper
 	 */
 	public void setDOI(String DOI) {
 		this.DOI = DOI;
 	}
}
