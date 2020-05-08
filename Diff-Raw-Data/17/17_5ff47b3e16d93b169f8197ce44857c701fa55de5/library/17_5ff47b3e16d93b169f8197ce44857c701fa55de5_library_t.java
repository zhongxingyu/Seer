 
 public class library {
 	String address;
 	String hours;
	int numbofbooks=0;
	Book[] book;
 	void printAvailableBooks()
 	{
	
 	}
 	void borrowBook(String booktitle)
 	{
 	}
	void addBook(String booktitle)
	{
		book[numbofbooks]=new Book(booktitle);
		numbofbooks++;
	}
	
 }
