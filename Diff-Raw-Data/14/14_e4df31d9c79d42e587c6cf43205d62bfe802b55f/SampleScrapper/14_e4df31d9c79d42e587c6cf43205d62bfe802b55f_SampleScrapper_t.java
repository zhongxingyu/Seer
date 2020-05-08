 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 
 public class SampleScrapper {
 	
 	static List<PlayerRecord> twentyTwo = new ArrayList<PlayerRecord>();
 	static ConvertDAO dao = new ConvertDAO();
 	public static void main(String[] args) throws Exception {
 		System.out.println("Enter Cricinfo Match Scorecard URL: ");
 		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
 		Document doc = Jsoup.connect(br.readLine()).get();
 		Element inningsBat1 = doc.getElementById("inningsBat1");
 		Elements inningsRow = inningsBat1.getElementsByClass("inningsRow");
 		int count = addtoList(inningsRow);
 		Elements inningsTable = doc.getElementsByClass("inningsTable");//max of 3 for each team
		boolean firstInningsAllout = true;
 		if(count!=11)
		{
 			addDNB(inningsTable.get(1));
			firstInningsAllout= false;
		}
 		//11 players of team 1 added by now
 		Element inningsBat2 = doc.getElementById("inningsBat2");
 		inningsRow = inningsBat2.getElementsByClass("inningsRow");
 		count = addtoList(inningsRow);
 		if(count!=11)
		{
			if(!firstInningsAllout)
				addDNB(inningsTable.get(5));
			else
				addDNB(inningsTable.get(4));
		}
 		
 		//dismissal
 		inningsRow = inningsBat1.getElementsByClass("inningsRow");
 		for (int i=0;i<inningsRow.size()-2;i++) {
 			Element batsman = inningsRow.get(i);
 			Elements battingDismissal = batsman.getElementsByClass("battingDismissal");
 			updateDismissal(battingDismissal.text(),i);
 		}		
 		
 		inningsRow = inningsBat2.getElementsByClass("inningsRow");
 		for (int i=0;i<inningsRow.size()-2;i++) {
 			Element batsman = inningsRow.get(i);
 			Elements battingDismissal = batsman.getElementsByClass("battingDismissal");
 			updateDismissal(battingDismissal.text(),i+11);
 		}		
 		
 		//bowling
 		Element inningsBowl1 = doc.getElementById("inningsBowl1");
 		inningsRow = inningsBowl1.getElementsByClass("inningsRow");
 		updateBowlingFigures(inningsRow);
 		
 		Element inningsBowl2 = doc.getElementById("inningsBowl2");
 		inningsRow = inningsBowl2.getElementsByClass("inningsRow");
 		updateBowlingFigures(inningsRow);
 		System.out.println("\n\n");
 		for(int i=0;i<twentyTwo.size();i++)
 		{
 			twentyTwo.get(i).print();
 			System.out.println();
 		}
 		
 		
 	}
 
 	private static void updateBowlingFigures(Elements inningsRow) {
 		for (int i=0;i<inningsRow.size();i++) {
 			int bowlingDetails[] = new int[5];
 			Element bowler = inningsRow.get(i);
 			Elements bowlerName = bowler.getElementsByTag("a");
 			int tempID = lookupIDUnder22(bowlerName.text());
 			Elements bowlDetails = bowler.getElementsByClass("bowlingDetails");
 			float oversBowled = Float.parseFloat(bowlDetails.get(0).text());
 			bowlingDetails[0] = Integer.parseInt(bowlDetails.get(1).text());//maidens
 			bowlingDetails[1] = Integer.parseInt(bowlDetails.get(2).text());//R
 			bowlingDetails[2] = Integer.parseInt(bowlDetails.get(3).text());//W
 			String input = bowlDetails.get(5).text();
 			if(!input.equals(""))
 			{
 				input=input.replaceAll("\\(", "");
 				input=input.replaceAll("\\)", "");
 				String[] extra = input.split(", ");
 				if(extra.length>1)
 				{
 					extra[0]=extra[0].replace("nb", "");
 					bowlingDetails[4]=Integer.parseInt(extra[0]);
 					extra[1]=extra[1].replace("w", "");
 					bowlingDetails[3]=Integer.parseInt(extra[1]);
 				}
 				else
 				{
 					if(extra[0].contains("w"))
 					{
 						extra[0]=extra[0].replace("w", "");
 						bowlingDetails[3]=Integer.parseInt(extra[0]);
 					}
 					else
 					{
 						extra[0]=extra[0].replace("nb", "");
 						bowlingDetails[4]=Integer.parseInt(extra[0]);
 					}
 				}
 			}
 			twentyTwo.get(tempID).oversBowled= oversBowled;
 			twentyTwo.get(tempID).bowlingDetails=bowlingDetails;
 		}
 	}
 
 	private static int lookupIDUnder22(String text) {
 		for(int i=0;i<twentyTwo.size();i++)
 		{
 			PlayerRecord pr = twentyTwo.get(i);
 			if(pr.playerName.contains(text))
 				return i;
 		}
 		return -1;
 	}
 
 	private static void updateDismissal(String input,int i) {
 		String[] dismissal = input.split(" ", 2);
 		String[] nextSplit;
 		int tempID;
 		switch(dismissal[0])
 		{
 			case "run" :
 				//TODO : how to handle this?
 				twentyTwo.get(i).dismissal="runout";
 				incRunouts(dismissal[1]);
 				break;
 			case "not" :
 				twentyTwo.get(i).dismissal="NO";
 				break;
 			case "lbw" :
 				twentyTwo.get(i).dismissal="lbw";
 				nextSplit = dismissal[1].split("b ");
 				tempID=lookupPlayerIDInMatch(nextSplit[0]);
 				twentyTwo.get(i).wicketTaker = tempID;
 				break;
 			case "st" :
 				twentyTwo.get(i).dismissal="stumped";
 				nextSplit = dismissal[1].split(" b ");
 				tempID=lookupPlayerIDInMatch(nextSplit[1]);
 				incStumpings(nextSplit[0].replaceFirst("", ""));	
 				twentyTwo.get(i).wicketTaker = tempID;
 				break;
 			case "c" :
 				//handle '&'
 				twentyTwo.get(i).dismissal="caught";
 				nextSplit = dismissal[1].split(" b ");
 				if(nextSplit[0].equals("&"))
 					incCatches(nextSplit[1]);
 				else
 					incCatches(nextSplit[0].replaceFirst("", ""));
 				tempID=lookupPlayerIDInMatch(nextSplit[1]);
 				twentyTwo.get(i).wicketTaker = tempID;			
 				break;
 			case "b" :
 				twentyTwo.get(i).dismissal="bowled";
 				tempID=lookupPlayerIDInMatch(dismissal[1]);
 				twentyTwo.get(i).wicketTaker = tempID;	
 				break;
 		}
 	}
 
 	private static void incRunouts(String input) {
 		input=input.replaceAll("\\(", "");
 		input=input.replaceAll("\\)", "");
 		input=input.replaceAll("out ", "");
 		String[] runout = input.split("/");
 		for(int i=0;i<runout.length;i++)
 		{
 			int tempID = lookupIDUnder22(runout[i].replaceFirst("", ""));
 			twentyTwo.get(tempID).runouts++;
 		}
 	}
 
 	private static void incCatches(String string) {
 		for(int i=0;i<twentyTwo.size();i++)
 		{
 			PlayerRecord pr = twentyTwo.get(i);
 			if(pr.playerName.contains(string))
 				pr.catches++;
 		}
 	}
 
 	private static void incStumpings(String string) {
 		for(int i=0;i<twentyTwo.size();i++)
 		{
 			PlayerRecord pr = twentyTwo.get(i);
 			if(pr.playerName.contains(string))
 				pr.stumps++;
 		}
 	}
 
 	private static int lookupPlayerIDInMatch(String string) {
 		for(int i=0;i<twentyTwo.size();i++)
 		{
 			PlayerRecord pr = twentyTwo.get(i);
 			if(pr.playerName.contains(string))
 				return pr.playerID;
 		}
 		return -1;
 		
 	}
 
 	private static void addDNB(Element dNB) {
 		Elements inningsDetails = dNB.getElementsByTag("a");
 		for(int i=0;i<inningsDetails.size();i++)
 		{
 			PlayerRecord player = new PlayerRecord();
 //			System.out.println(inningsDetails.get(i).text());
 			player.playerName = inningsDetails.get(i).text();
 			player.playerID = dao.lookupID(player.playerName);
 			twentyTwo.add(player);
 		}
 		
 	}
 
 	private static int addtoList(Elements inningsRow) {
 		int count=0;
 		for (int i=0;i<inningsRow.size()-2;i++) {
 			count++;
 			Element batsman = inningsRow.get(i);
 			PlayerRecord player = new PlayerRecord();
 			Elements batsmanName = batsman.getElementsByTag("a");
 			Elements wKC = batsman.getElementsByClass("playerName");
 			if(wKC.get(0).text().contains("*"))
 				player.isCap=true;
 			if(wKC.get(0).text().contains(""))
 				player.isWK=true;
 			player.playerName = batsmanName.text();
 			player.playerID = dao.lookupID(player.playerName);
 			Elements runDetails = batsman.getElementsByClass("battingRuns");
 			int runs[] = new int[5];
 			runs[0] = Integer.parseInt(runDetails.get(0).text());
 			runDetails = batsman.getElementsByClass("battingDetails");
 			runs[1] = Integer.parseInt(runDetails.get(0).text());
 			runs[2] = Integer.parseInt(runDetails.get(1).text());
 			runs[3] = Integer.parseInt(runDetails.get(2).text());
 			runs[4] = Integer.parseInt(runDetails.get(3).text());
 			player.runDetails=runs;
 			twentyTwo.add(player);
 		}
 		return count;
 	}
 
 }
