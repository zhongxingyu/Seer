 
 public class Main {
 	
 	public static void main(String args[])
	{			
 		String[] codes = {"Open","High","Low","Close","Volume"};
 		
 		Company[] companies = 
 		{new Company("MSFT"), new Company("GOOG"), new Company("AAPL"), 
 			new Company("IMAX"), new Company("CSCO"), new Company("IBM"), 
 			new Company("ORCL"), new Company("MCD")};
 		
 		for(int i = 0; i < companies.length; i++){
 			String fileName = "D:\\FMI\\Magistratura\\Izvli4ane_na_informaciq\\GoogleGetDataProject\\HistoricPrices\\"
 					+ companies[i].getNasdaqCode()+ ".csv";
 			companies[i].loadfromCSV(fileName);
 		}
 		
 		Statistics statistics = new Statistics();
 		for( int i = 0; i < companies.length; i++)
 			for( int j = i; j < companies.length;j++)
 				for( int k = 0; k < codes.length; k++)
 					for( int l = 0; l < codes.length; l++){
 				double corr = statistics.Correlation(companies[i].getValue(codes[k]), companies[j].getValue(codes[l]));
 				
 				System.out.print(companies[i].getNasdaqCode() + " " + codes[k] + " ");
 				System.out.print(companies[j].getNasdaqCode() + " " + codes[l] + " ");
 				System.out.println("correlation between is: " + corr);
 				
 				double[] regres = statistics.determineRegressionBetweenTwoVariables(companies[i].getValue(codes[k]), companies[j].getValue(codes[l]));
 				
 				System.out.print(companies[i].getNasdaqCode() + " " + codes[k] + " ");
 				System.out.print(companies[j].getNasdaqCode() + " " + codes[l] + " ");
 
 				System.out.println("Regression is: " + regres[0] + " " + regres[1] );	
 			}
 	
 	}
 }
 
 
