 package Java;
 
 import java.util.List;
 
 public class Book{
 	private String features;
 	public static void main(String[] args){
 		Book a=new Book("FICCOMROMARC_3_");
 		Book b=new Book("NONCOMROMABA_3_");
		System.out.println(a.getMatchCoeff(b));
 	}
 	public Book(String features){
 		this.features=features;
 	}
 	public double getMatchCoeff(Book other){
		String[] a=new String[features.length()];
		String[] b=new String[features.length()];
 		for(int i=0;i<features.length();i+=3){
 			a[i/3]=features.substring(i,i+3);
 			b[i/3]=other.features.substring(i,i+3);
 		}
 		int count=0;
 		for(String feat:a)
 			for(String otherFeat:b)
 				if(feat.equals(otherFeat))
 					count++;
 		return count/(double)a.length;
 	}
	public double[] getFitCoefficients(List<Book> books){
 		double[] values=new double[books.size()];
 		for(int i=0;i<books.size();i++)
 			for(int j=i+1;j<books.size();j++){
 				double match=books.get(i).getMatchCoeff(books.get(j));
 				values[i]+=match;
 				values[j]+=match;
 			}
 		return values;
 	}
	public void removeOutliers(List<Book> book){
 		double[] values=getFitCoefficients(book);
 		double avg=0;
 		for(double val:values)
 			avg+=val;
 		avg/=values.length;
		for(int i=values.length;i>=0;i--)
 			if(values[i] < 0.5*avg)
 				book.remove(values[i]);
 	}
 }
