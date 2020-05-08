 package Transactions;
 
 public class TestData {
 
 	String callNumber, title, mainAuthor, publisher, subject, status;
 	int isbn, year;
 	
 	private Transactions trans;
 	
 	public TestData() {
 		trans = new Transactions();
 	}
 	
 	public void insertBookData() {
 		trans.insertBook("QA76.73.J38 2004", 1590591232, "Advanced Java Game Programming", "David Wallace Croft", "Berkley Press", 2004);
 		trans.insertBook("QA39.3.E65 2004", 534490964, "Discrete Mathematics with Applications", "Susanna S. Epp", "Thomson-Brooks/Cole", 2004);
 		trans.insertBook("QA76.9.A43 G67 2002", 471383651, "Algorithm Design", "Michael T. Goodrich", "Wiley", 2002);
 		trans.insertBook("PR6068.O93 Z68 2003", 403912645, "Re-reading Harry Potter", "Suman Gupta", "Palgrave Macmillan", 2003);
 		trans.insertBook("GE160.A35 O35 2010", 781617288, "Africa: the driest continent", "Abdeen Mustafa Omer", "Nova Science", 2010);
 		trans.insertBook("QC990.I4 S55 2010", 176580597, "Global warming: India's response to climate change, disasster mitigation and adaptation", "Pawan Sikka", "Uppal Publishing House", 2010);
 		trans.insertBook("QA76.73.J38 L25 2000", 534366910, "Java: a framework for program design and data structures", "Martin Osborne", "Brooks/Cole", 2000);
 		trans.insertBook("QA76.73.C153 M268 2011", 471383651, "C++ programming: from problem analysis to program design", "D.S. Malik", "Course Technology", 2011);
 		trans.insertBook("QA297.A748 2011", 898719976, "A first course in numerical methods", "U.M. Ascher", "Society for Industrial and Applied Mathematics", 2011);
 		trans.insertBook("QA76.6.M3585 2009", 0071624724, "Programming: a beginner's guide", "Richard Mansfield", "McGraw-Hill", 2009);
 	}
 	
 	public void insertHasSubjData() {
 		trans.testInsertHasSubject("QA76.73.J38 2004","java");
 		trans.testInsertHasSubject("QA76.73.J38 2004","programming");
 		trans.testInsertHasSubject("QA76.73.J38 2004","computer games");
 		trans.testInsertHasSubject("QA76.73.J38 2004","computer science");
 		
 		trans.testInsertHasSubject("QA39.3.E65 2004","discrete mathematics");
 		
 		trans.testInsertHasSubject("QA76.9.A43 G67 2002","computer science");
 		trans.testInsertHasSubject("QA76.9.A43 G67 2002","programming");
 		trans.testInsertHasSubject("QA76.9.A43 G67 2002","algorithms");
 		
 		trans.testInsertHasSubject("PR6068.O93 Z68 2003","harry potter");
 		
 		trans.testInsertHasSubject("GE160.A35 O35 2010","africa");
 		trans.testInsertHasSubject("GE160.A35 O35 2010","continent");
 		
 		trans.testInsertHasSubject("QC990.I4 S55 2010","global warming");
 		trans.testInsertHasSubject("QC990.I4 S55 2010","india");
 		trans.testInsertHasSubject("QC990.I4 S55 2010","climate change");
 		
 		trans.testInsertHasSubject("QA76.73.J38 L25 2000","java");
 		trans.testInsertHasSubject("QA76.73.J38 L25 2000","computer science");
 		trans.testInsertHasSubject("QA76.73.J38 L25 2000","programming");
 		
 		trans.testInsertHasSubject("QA76.73.C153 M268 2011","c++");
 		trans.testInsertHasSubject("QA76.73.C153 M268 2011","computer science");
 		trans.testInsertHasSubject("QA76.73.C153 M268 2011","programming");
 		
 		trans.testInsertHasSubject("QA297.A748 2011","numerical methods");
 		trans.testInsertHasSubject("QA297.A748 2011","computer science");
 		
 		trans.testInsertHasSubject("QA76.6.M3585 2009","computer science");
 		trans.testInsertHasSubject("QA76.6.M3585 2009","programming");		
 	}
 	
 	public void insertHasAuthorData() {
 		trans.testInsertHasAuthor("QA76.73.J38 2004", "David Wallace Croft");
 		trans.testInsertHasAuthor("QA39.3.E65 2004", "Susanna S. Epp");
 		trans.testInsertHasAuthor("QA76.9.A43 G67 2002", "Michael T. Goodrich");
 		trans.testInsertHasAuthor("PR6068.O93 Z68 2003", "Suman Gupta");
 		trans.testInsertHasAuthor("GE160.A35 O35 2010", "Abdeen Mustafa Omer");
 		trans.testInsertHasAuthor("QC990.I4 S55 2010", "Pawan Sikka");
 		trans.testInsertHasAuthor("QA76.73.J38 L25 2000", "Martin Osborne");
 		trans.testInsertHasAuthor("QA76.73.C153 M268 2011", "D.S. Malik");
 		trans.testInsertHasAuthor("QA297.A748 2011", "U.M. Ascher");
 		trans.testInsertHasAuthor("QA76.6.M3585 2009", "Richard Mansfield");
 	}
 	
 	
 	public void insertBorrowerData() {
 		trans.insertBorrowerForTestData(100000, "786362", "John Smith", "200 Beach Ave", "6048233829", "jsmith@mail.com", "12313849", "2020/01/01", "student");
 		trans.insertBorrowerForTestData(100001, "392024", "Jane Clark", "3829 Sunset Dr", "7882340101", "clarkj@abc.com", "48393202", "2018/05/27", "student");
 		trans.insertBorrowerForTestData(100002, "289238", "Calvin Klein", "100 Church St", "7889112020", "ck@mail.com", "39201391", "2013/01/05", "student");
 		trans.insertBorrowerForTestData(100003, "992928", "Mickey Mouse", "90 Disney Dr", "2507892343", "mmouse@cartoon.com", "38291111", "2020/02/25", "student");
 		trans.insertBorrowerForTestData(100004, "123123", "Donald Duck", "88 Space Mountain Rd", "5198093456", "quackers@rus.com", "10020030", "2019/10/10", "student");
 		
 		trans.insertBorrowerForTestData(100005, "234213", "Prof Plum", "111 Candlestick Dr", "2322344686", "pplum@instudy.com", "281835829", "2016/10/12", "faculty");
 		trans.insertBorrowerForTestData(100006, "d8s3k2", "John Brown", "234 River Rd", "2342432879", "jbrown@instudy.com", "465485447", "2013/03/21", "faculty");
 		trans.insertBorrowerForTestData(100007, "s9sfd8", "Jim Flack", "565 Taylor Way", "8881233829", "flack@up.com", "343532342", "2013/11/05", "faculty");
 		trans.insertBorrowerForTestData(100008, "32kj42", "Barbara Paul", "2342 Marine Dr", "8272347842", "paulb@here.com", "345364534", "2014/05/10", "faculty");
 		trans.insertBorrowerForTestData(100009, "l23424", "Ian Fleming", "3732 Granville St", "4839202010", "iflem@jb.com", "123123123", "2015/03/15", "faculty");
 		
 		trans.insertBorrowerForTestData(100010, "234213", "Donna Clark", "546 Lakeway Dr", "6048729312", "dclark@univ.edu", "234165874", "2014/10/02", "staff");
 		trans.insertBorrowerForTestData(100011, "d8s3k2", "John Green", "123 Hill Rd", "2342234234", "jgreen@why.com", "978351756", "2015/03/03", "staff");
 		trans.insertBorrowerForTestData(100012, "s9sfd8", "Gail Smith", "435 Lucky Way", "2342235556", "gsmith@sky.com", "393882573", "2013/12/31", "staff");
 		trans.insertBorrowerForTestData(100013, "32kj42", "Bob Park", "5453 Park Dr", "1028282737", "ballpark@mail.com", "112934228", "2013/05/30", "staff");
 		trans.insertBorrowerForTestData(100014, "l23424", "Lewis Carrol", "352 Sewer St", "2349172732", "lcarroll@mail.co.uk", "445889282", "2015/01/01", "staff");
 	} 
 	
 	public void insertBookCopyData() {
 		trans.insertBookCopy("QA297.A748 2011", 1, "out");
 		trans.insertBookCopy("QA297.A748 2011", 2, "out");
 		trans.insertBookCopy("QA297.A748 2011", 3, "on-hold");
 		trans.insertBookCopy("QA39.3.E65 2004", 1, "on-hold");
 		trans.insertBookCopy("QA76.9.A43 G67 2002", 1, "in");
 		trans.insertBookCopy("QA76.9.A43 G67 2002", 2, "in");
 		trans.insertBookCopy("PR6068.O93 Z68 2003", 1, "in");
 		trans.insertBookCopy("GE160.A35 O35 2010", 1, "on-hold");
 		trans.insertBookCopy("GE160.A35 O35 2010", 2, "on-hold");
 		trans.insertBookCopy("QC990.I4 S55 2010", 1, "out");
 		trans.insertBookCopy("QA76.73.J38 L25 2000", 1, "out");
 		trans.insertBookCopy("QA76.73.C153 M268 2011", 1, "on-hold");
 		trans.insertBookCopy("QA76.73.C153 M268 2011", 2, "out");
 		trans.insertBookCopy("QA76.73.C153 M268 2011", 3, "out");
 		trans.insertBookCopy("QA76.73.C153 M268 2011", 4, "out");
 		trans.insertBookCopy("QA76.73.C153 M268 2011", 5, "out");
 		trans.insertBookCopy("QA76.6.M3585 2009", 1, "in");
 		trans.insertBookCopy("QA76.6.M3585 2009", 2, "out");
 	}
 	
 	public void insertBorrowingData() {
 		trans.insertBorrowingForTestData(1000,100000,"QA297.A748 2011", 1, "2012/09/03",null);
 		trans.insertBorrowingForTestData(1001,100001,"QA297.A748 2011", 2, "2012/10/08",null);
 		trans.insertBorrowingForTestData(1002,100000,"QC990.I4 S55 2010", 1, "2012/10/01","2012/11/12");
 		trans.insertBorrowingForTestData(1003,100000,"QA76.73.J38 L25 2000", 1, "2012/10/01","2012/11/12");
 		trans.insertBorrowingForTestData(1004,100007,"QA76.73.C153 M268 2011", 2, "2012/10/28",null);
 		trans.insertBorrowingForTestData(1005,100005,"QA76.73.C153 M268 2011", 3, "2012/07/16","2012/08/06");
 		trans.insertBorrowingForTestData(1006,100009,"QA76.73.C153 M268 2011", 4, "2012/09/17","2012/10/08");
 		trans.insertBorrowingForTestData(1007,100013,"QA76.73.C153 M268 2011", 5, "2012/10/15","2012/11/05");
 		trans.insertBorrowingForTestData(1008,100000,"QA76.6.M3585 2009", 2, "2012/09/03",null);
 		trans.insertBorrowingForTestData(1009,100010,"GE160.A35 O35 2010",1, "2011/01/14","2011/01/20");
 		trans.insertBorrowingForTestData(1010,100010,"QA39.3.E65 2004",1, "2011/03/14","2011/03/21");
 		trans.insertBorrowingForTestData(1011,100010,"QA39.3.E65 2004",1, "2010/03/14","2010/04/01");
 	}
 	
 	public void insertHoldReqData() {
 		trans.insertHoldReqForTestData(1000,"QA297.A748 2011",100002, "2012/11/20");
 		trans.insertHoldReqForTestData(1001,"QA39.3.E65 2004",100004, "2012/11/14");
 		trans.insertHoldReqForTestData(1002,"GE160.A35 O35 2010",100003, "2012/11/10");
 		trans.insertHoldReqForTestData(1003,"GE160.A35 O35 2010",100008, "2012/11/12");
 		trans.insertHoldReqForTestData(1004,"QA76.73.C153 M268 2011",100002, "2012/10/31");
 	}
 	
 	public void insertFineData() {
 		trans.insertFineForTestData(1000,3,"2011/09/03", "2012/09/10", 1000);
 		trans.insertFineForTestData(1001,1,"2012/10/06", "2012/10/07", 1000);
 		
 		trans.insertFineForTestData(1002,115,"2012/10/01", null, 1008);
 		
 		trans.insertFineForTestData(1003,4,"2012/11/01", null, 1005);
 		
 		trans.insertFineForTestData(1004,1,"2012/08/21", null, 1004);
 		trans.insertFineForTestData(1005,13,"2012/11/17", null, 1004);	
 	}
 	
 	public void insertBorrowType()
 	{
 		trans.insertBorrowerType("student", "14");
 		trans.insertBorrowerType("faculty", "84");
 		trans.insertBorrowerType("staff", "42");
 	}
 }
 
