 package com.seventhirtyfive;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.Collections;
 import java.util.Vector;
 
 public class AUFileLoader {
 	public static final int BATTER_LOAD = 1;
 	public static final int PITCHER_LOAD = 2;
 	public static final int TEAM_LOAD = 3;
 	
 	private String mFilePath;
 	private int mLoadType;
 	private int mYearToLoad;
 	private Debug735 mDebug = PlayerRater.getDebugger();
 	private Debug735 mPitcherWindow = PlayerRater.getPitcherWindow();
 	private Debug735 mTeamWindow = PlayerRater.getTeamWindow();
 	private Vector<AUBatter> mBatterList = new Vector<AUBatter>();
 	private Vector<AUPitcher> mPitcherList = new Vector<AUPitcher>();
 	
 	public AUFileLoader( String filepath, int loadType, int yearToLoad ) {
 		mFilePath = filepath;
 		mLoadType = loadType;
 		mYearToLoad = yearToLoad;
 	}
 	
 	public Vector<AUPlayer> get2009RookieHitters() {
 		File f = new File(PlayerRater.BATTER_FILE_PREVIOUSYEAR);
 		if (!f.exists()) {
 			mDebug.log(PlayerRater.BATTER_FILE_PREVIOUSYEAR+" doesn't exist");
 			return null;
 		}
 		Vector<AUPlayer> vector = new Vector<AUPlayer>();
 		try {
 			BufferedReader input = new BufferedReader(new FileReader(f));
 			// read twice to get the header out of the way
 			input.readLine();
 			input.readLine();
 			
 			// parse the players
 			String player;
 			while ((player = input.readLine()) != null) {
 				String [] parsedLine = player.split("\t");
 				
 				String last = getLastName(parsedLine[0]);
 				String first = getFirstName(parsedLine[0]);
 				
 				int abs = Integer.parseInt(parsedLine[3]);
 	            if (abs <= 100) {
 	            	// found a rookie, add to the list
 	            	AUPlayer newPlayer = new AUPlayer(first, last, "OF");
 					vector.add(newPlayer);
 	            }
 			}
 
 		}catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return vector;
 	}
 	
 	public Vector<AUBatter> getRookieHitters( String filepath ) {
 		File f = new File(filepath);
 		if (!f.exists()) {
 			mDebug.log(filepath+" doesn't exist");
 			return null;
 		}
 		Vector<AUBatter> vector = new Vector<AUBatter>();
 		try {
 			BufferedReader input = new BufferedReader(new FileReader(f));
 			// read twice to get the header out of the way
 			input.readLine();
 			input.readLine();
 			
 			// parse the players
 			String player;
 			while ((player = input.readLine()) != null) {
 				String [] parsedLine = player.split("\t");
 				
 				String last = getLastName(parsedLine[0]);
 				String first = getFirstName(parsedLine[0]);
 				
 				int abs = Integer.parseInt(parsedLine[2]);
 	            if (abs * 3 < 100) {
 	            	// found a rookie, add to the list
 					AUBatter newPlayer = new AUBatter(first, last, "OF");
 					vector.add(newPlayer);
 	            }
 			}
 
 		}catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return vector;
 	}
 
 	public Vector<AUPlayer> get2009RookiePitchers() {
 		File f = new File(PlayerRater.PITCHER_FILE_PREVIOUSYEAR);
 		if (!f.exists()) {
 			mDebug.log(PlayerRater.PITCHER_FILE_PREVIOUSYEAR+" doesn't exist");
 			return null;
 		}
 		Vector<AUPlayer> vector = new Vector<AUPlayer>();
 		try {
 			BufferedReader input = new BufferedReader(new FileReader(f));
 			// read twice to get the header out of the way
 			input.readLine();
 			input.readLine();
 			
 			// parse the players
 			String player;
 			while ((player = input.readLine()) != null) {
 				String [] parsedLine = player.split("\t");
 				
 				String last = getLastName(parsedLine[0]);
 				String first = getFirstName(parsedLine[0]);
 				
 				double innings = Double.parseDouble(parsedLine[3]);
 	            if (innings <= 50.0) {
 	            	// found a rookie, add to the list
 	            	AUPlayer newPlayer = new AUPlayer(first, last, "SP");
 					vector.add(newPlayer);
 	            }
 			}
 
 		}catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return vector;
 	}
 	
 	public Vector<AUPitcher> getRookiePitchers( String filepath ) {
 		File f = new File(filepath);
 		if (!f.exists()) {
 			mDebug.log(filepath+" doesn't exist");
 			return null;
 		}
 		Vector<AUPitcher> vector = new Vector<AUPitcher>();
 		try {
 			BufferedReader input = new BufferedReader(new FileReader(f));
 			// read twice to get the header out of the way
 			input.readLine();
 			input.readLine();
 			
 			// parse the players
 			String player;
 			while ((player = input.readLine()) != null) {
 				String [] parsedLine = player.split("\t");
 				
 				String last = getLastName(parsedLine[0]);
 				String first = getFirstName(parsedLine[0]);
 				
 				double innings = Double.parseDouble(parsedLine[2]);
 	            if (innings * 3 < 50.0) {
 	            	// found a rookie, add to the list
 	            	AUPitcher newPlayer = new AUPitcher(first, last, "SP");
 					vector.add(newPlayer);
 	            }
 			}
 
 		}catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return vector;
 	}
 
 	/**
 	 * This file takes a csv of players with a common position
 	 * 
 	 * @param filepath - Path of the eligibility file
 	 * @param position - Position to set
 	 * @return
 	 */
 	public Vector<AUPlayer> getEligiblePlayers( String filepath, String position ) {
 		File f = new File(filepath);
 		if (!f.exists()) {
 			mDebug.log(filepath+" doesn't exist");
 			return null;
 		}
 		Vector<AUPlayer> vector = new Vector<AUPlayer>();
 		try {
 			BufferedReader input = new BufferedReader(new FileReader(f));
 			// read twice to get the header out of the way
 			input.readLine();
 			input.readLine();
 			
 			// parse the players
 			String player;
 			while ((player = input.readLine()) != null) {
 				String [] parsedLine = player.split("\t");
 				
 				String last = getLastName(parsedLine[0]);
 				String first = getFirstName(parsedLine[0]);
 				
 				AUPlayer newPlayer = new AUPlayer(first, last, position);
 				vector.add(newPlayer);
 			}
 
 		}catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return vector;
 	}
 	
 	public boolean load() {
 		File f = new File(mFilePath);
 		if (!f.exists()) {
 			mDebug.log(mFilePath+" doesn't exist");
 			return false;
 		}
 		try {
 			BufferedReader input = new BufferedReader(new FileReader(f));
 			// read once to get the header out of the way
 			input.readLine();
 			switch (mLoadType) {
 				case BATTER_LOAD:
 					return loadBatters(input);
 				case PITCHER_LOAD:
 					return loadPitchers(input);
 				case TEAM_LOAD:
 					return loadTeams(input);
 			}
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return false;
 	}
 	
 	private boolean loadTeams(BufferedReader reader) throws IOException {
 		// throw away first line
 		String player = reader.readLine();            // drop this line
 
 		boolean loadingBatters = true;
 		AULeague league = AULeague.getInstance();
 		// parse the teams
 		while ((player = reader.readLine()) != null) {
 			String [] parsedLine = player.split("\t");
 			
 			if (parsedLine[0].startsWith("Team") || parsedLine[0].startsWith(PlayerRater.LASTYEAR)) {
 				reader.readLine();		/// ignore this
 				loadingBatters = false;
 				continue;
 			}
 			if (loadingBatters && parsedLine.length > 2) {
 				String teamName = parsedLine[0];
 				AUTeam team = new AUTeam(teamName);
 				AUBatterStats bStats = team.getBatterStats();
 				bStats.setAb(Integer.parseInt(parsedLine[1]));
 				bStats.setRuns(Integer.parseInt(parsedLine[2]));
 				// parsedLine[3] 	total hits (calculated)
 				bStats.set1B(Integer.parseInt(parsedLine[4]));
 				bStats.set2B(Integer.parseInt(parsedLine[5]));
 				bStats.set3B(Integer.parseInt(parsedLine[6]));
 				bStats.setHr(Integer.parseInt(parsedLine[7]));
 				bStats.setRbi(Integer.parseInt(parsedLine[8]));
 				bStats.setBb(Integer.parseInt(parsedLine[9]));
 				// parsedLine[10]	Strikeouts
 				bStats.setSb(Integer.parseInt(parsedLine[11]));
 				// parsedLine[12]	Caught stealing
 				// parsedLine[13]	OBP
 				// parsedLine[14]	SLG
 				// parsedLine[15]	Rank
 				league.addTeam(team);
 			} else {
 				// batting is done, look up the team, then load the pitching stats
 				AUTeam team = league.getTeam(parsedLine[0]);
 				AUPitcherStats pStats = team.getPitcherStats();
 				pStats.setInnings(Float.parseFloat(parsedLine[1]));
 				// parsedLine[2]	Games started
 				// parsedLine[3]	Quality Starts
 				// parsedLine[4]	Complete games
 				pStats.setWins(Integer.parseInt(parsedLine[5]));
 				pStats.setLosses(Integer.parseInt(parsedLine[6]));
 				pStats.setSaves(Integer.parseInt(parsedLine[7]));
 				// parsedLine[8]	Blown Saves
 				pStats.setStrikeouts(Integer.parseInt(parsedLine[9]));
 				pStats.setWalks(Integer.parseInt(parsedLine[10]));
 				pStats.setHitsAgainst(Integer.parseInt(parsedLine[11]));
 				pStats.setEra(Float.parseFloat(parsedLine[12]));
 				// parsedLine[13]	WHIP (calculated)
 			}
 		}
 		
 		mTeamWindow.addString(AUTeam.getBatterHeader());
 		for(int i = 0; i < league.getNumberOfTeams(); i++) {
 			mTeamWindow.addString(league.getBatterLine(i));
 		}
 		mTeamWindow.addString(AUTeam.getPitcherHeader());
 		for(int i = 0; i < league.getNumberOfTeams(); i++) {
 			mTeamWindow.addString(league.getPitcherLine(i));
 		}
 		System.out.println("ERA:"+league.getEraAverage());
 		System.out.println("Whip:"+league.getWhipAverage());
 		System.out.println("Strikeouts:"+league.getKAverage());
 		System.out.println("Innings:"+league.getInnAverage());
 		System.out.println("Wins:"+league.getWinAverage());
 		mTeamWindow.update();
 
 		return true;
 	}
 	
 	private boolean loadPitchers(BufferedReader reader) throws IOException {
 		// throw away first line
 		String player = reader.readLine();            // drop this line
 		mPitcherWindow.addString(AUPitcher.getPitcherHeader());
 		
 		// parse the players
 		while ((player = reader.readLine()) != null) {
 			String [] parsedLine = player.split("\t");
 			
 			// Last, First POS TEAM
 			String last = getLastName(parsedLine[0]);
 			String first = getFirstName(parsedLine[0]);
 			String position = getPosition(parsedLine[0]);
 
 			// TODO: this is wrong, I shouldn't be creating a new object, I should
 			// be looking it up in a hashmap...
 			AUPitcher pitcher = new AUPitcher(first, last, position);
 			pitcher.setYear(mYearToLoad);
             String team = getTeam(parsedLine[0]);
             pitcher.setTeam(AUMlbTeamFactory.getTeam(true, team));
 			// parsedLine[1];       AU Team Name (not used)
             AUPitcherStats stats = pitcher.getStats(mYearToLoad);
            stats.setInnings(Float.parseFloat(parsedLine[1]));  // 2012 added a dash, all values slide up
            // parsedLine[2] 		Games Started
            // parsedLine[3] 		Quality starts
            // parsedLine[4]		Complete games
            stats.setWins(Integer.parseInt(parsedLine[5]));
            stats.setLosses(Integer.parseInt(parsedLine[6]));
            stats.setSaves(Integer.parseInt(parsedLine[7]));
            // parsedLine[8]		Blown saves
            stats.setStrikeouts(Integer.parseInt(parsedLine[9]));
            stats.setWalks(Integer.parseInt(parsedLine[10]));
            stats.setHitsAgainst(Integer.parseInt(parsedLine[11]));
             // Only set the ERA if innings is > 0
             if (stats.getInnings()>0.0f) {
                stats.setEra(Float.parseFloat(parsedLine[12]));
             }
             // parsedLine[14]		Whip(Calculated)
             
             mPitcherList.add(pitcher);
 		}
 
         if (mYearToLoad == AUPlayer.YEAR_2009) {
         	
         	// eligibility check
 			checkPitcherEligibility(getEligiblePlayers(PlayerRater.mStartingPitcherFile, "SP"), "SP", AUPlayer.AUPlayerPosition.STARTING_PITCHER);
 			checkPitcherEligibility(getEligiblePlayers(PlayerRater.mReliefPitcherFile, "RP"), "RP", AUPlayer.AUPlayerPosition.RELIEF_PITCHER);
 			
 			// rookie check
 			checkForRookiePitchers(getRookiePitchers(PlayerRater.PITCHER_FILE_3YEAR_AVG), get2009RookiePitchers());
 			
             AUPitcher.SORT_TYPE = AUPitcher.SORT_PRICE;
     		System.out.println("PITCHER: Sort by Price");
     		Collections.sort(mPitcherList);
 
     		PlayerRater.getInstance().getPitcherStream().write("STARTING PITCHERS\n".getBytes());
 			outputPitcherPosition( AUPlayer.AUPlayerPosition.STARTING_PITCHER);
     		PlayerRater.getInstance().getPitcherStream().write("\nRELIEF PITCHERS\n".getBytes());
 			outputPitcherPosition( AUPlayer.AUPlayerPosition.RELIEF_PITCHER);
     		PlayerRater.getInstance().getPitcherStream().write("\nPITCHERS\n".getBytes());
 			outputPitcherPosition( AUPlayer.AUPlayerPosition.PITCHER);
 			
     		for(int i = 0; i < mPitcherList.size(); i++) {
     			AUPitcher p = mPitcherList.get(i);
     			mPitcherWindow.addString(p.toString());
     		}
     		mPitcherWindow.update();
         }
 
 		return true;
 	}
 	
 	private boolean loadBatters(BufferedReader reader) throws IOException {
 		// throw away first line
 		String player = reader.readLine();            // drop this line
 		String output = AUBatter.getBatterHeader();
 		mDebug.addString(output);
 		
 		// parse the players
 		while ((player = reader.readLine()) != null) {
 			String [] parsedLine = player.split("\t");
 			
 			// Last, First POS TEAM
 			String last = getLastName(parsedLine[0]);
 			String first = getFirstName(parsedLine[0]);
 			String position = getPosition(parsedLine[0]);
 			AUBatter batter = new AUBatter(first, last, position);
 			batter.setYear(mYearToLoad);
             String team = getTeam(parsedLine[0]);
             batter.setTeam(AUMlbTeamFactory.getTeam(true, team));
 			// parsedLine[1];       AU Team Name (not used)
             AUBatterStats stats = batter.getStats(mYearToLoad);
             stats.setAb(Integer.parseInt(parsedLine[2]));
             stats.setRuns(Integer.parseInt(parsedLine[3]));
             // parsedLine[4];       Hits
             stats.set1B(Integer.parseInt(parsedLine[5]));
             stats.set2B(Integer.parseInt(parsedLine[6]));
             stats.set3B(Integer.parseInt(parsedLine[7]));
             stats.setHr(Integer.parseInt(parsedLine[8]));
             stats.setRbi(Integer.parseInt(parsedLine[9]));
             stats.setBb(Integer.parseInt(parsedLine[10]));
             // parsedLine[11]        Strikeouts
             stats.setSb(Integer.parseInt(parsedLine[12]));
             // parsedLine[13]       Caught Stealing
             // parsedLine[14]       Batting average
             // parsedLine[15]       On base
             // parsedLine[16]       Slugging
 			
             mBatterList.add(batter);
             //Debug735.PlayerRow pComp = new Debug735.PlayerRow(batter);
 			//mDebug.addComponent(pComp);
 		}
         
 		setOverallRanking();
 		
         if (mYearToLoad == AUPlayer.YEAR_2009) {
 			PlayerRater pr = PlayerRater.getInstance();
 
 			// eligibility check
 			checkBatterEligibility(getEligiblePlayers(PlayerRater.mCatcherFile, "C"), "C", AUPlayer.AUPlayerPosition.CATCHER);
 			checkBatterEligibility(getEligiblePlayers(PlayerRater.mFirstBaseFile, "1B"), "1B", AUPlayer.AUPlayerPosition.FIRSTBASE);
 			checkBatterEligibility(getEligiblePlayers(PlayerRater.mSecondBaseFile, "2B"), "2B", AUPlayer.AUPlayerPosition.SECONDBASE);
 			checkBatterEligibility(getEligiblePlayers(PlayerRater.mThirdBaseFile, "3B"), "3B", AUPlayer.AUPlayerPosition.THIRDBASE);
 			checkBatterEligibility(getEligiblePlayers(PlayerRater.mShortStopFile, "SS"), "SS", AUPlayer.AUPlayerPosition.SHORTSTOP);
 			checkBatterEligibility(getEligiblePlayers(PlayerRater.mOutfieldFile, "OF"), "OF", AUPlayer.AUPlayerPosition.OUTFIELD);
 			
 			// rookie check
 			checkForRookieBatters( getRookieHitters(PlayerRater.BATTER_FILE_3YEAR_AVG), get2009RookieHitters() );
 			
             AUBatter.SORT_TYPE = AUBatter.SORT_PRICE;
     		System.out.println("Sort by Price");
     		Collections.sort(mBatterList);
 
     		PlayerRater.getInstance().getBatterStream().write("CATCHERS\n".getBytes());
     		outputBatterPosition(AUPlayer.AUPlayerPosition.CATCHER);
     		PlayerRater.getInstance().getBatterStream().write("\nFIRST BASE\n".getBytes());
 			outputBatterPosition(AUPlayer.AUPlayerPosition.FIRSTBASE);
     		PlayerRater.getInstance().getBatterStream().write("\nSECOND BASE\n".getBytes());
 			outputBatterPosition(AUPlayer.AUPlayerPosition.SECONDBASE);
     		PlayerRater.getInstance().getBatterStream().write("\nTHIRD BASE\n".getBytes());
 			outputBatterPosition(AUPlayer.AUPlayerPosition.THIRDBASE);
     		PlayerRater.getInstance().getBatterStream().write("\nSHORTSTOP\n".getBytes());
 			outputBatterPosition(AUPlayer.AUPlayerPosition.SHORTSTOP);
     		PlayerRater.getInstance().getBatterStream().write("\nOUTFIELDERS\n".getBytes());
 			outputBatterPosition(AUPlayer.AUPlayerPosition.OUTFIELD);
     		PlayerRater.getInstance().getBatterStream().write("\nBATTERS\n".getBytes());
 			outputBatterPosition(AUPlayer.AUPlayerPosition.DESIGNATEDHITTER);
 			outputBatterPosition(AUPlayer.AUPlayerPosition.BATTER);
 
     		for(int i = 0; i < mBatterList.size(); i++) {
     			AUBatter b = mBatterList.get(i);
     		    //if (mBatterList.get(i).isEligible(AUPlayer.AUPlayerPosition.SECONDBASE)) {
     	            mDebug.addString(b.toString());
     		    //}
     		}
 			pr.closeStream(pr.getBatterStream());
     		mDebug.update();
         }
 		return false;
 	}
 	
 	private String getLastName(String s) {
 		String [] parse = s.split(" ");
 		String name = parse[0].replace("\"","");
 		String names[] = name.split(",");
 		if(names.length > 1) {
 			return names[1];
 		} else {
 			return names[0].replace(",", "");
 		}
 	}
 	private String getFirstName(String s) {
 		String [] parse = s.split(" ");
 		if (AUPlayer.isStringPosition(parse[2])) {
 		    return parse[1];
 		} else if (parse[0].contains(",")) {
 			String [] names = parse[0].split(",");
 			return names[0].replace("\"", "");
 		}
 		else {
 		    return parse[1]+" "+parse[2];
 		}
 	}
 	private String getPosition(String s) {
        String [] parse = s.split(" ");
        for( int i = 0; i < parse.length; i++ ){
            if (AUPlayer.isStringPosition(parse[i])) {
                return parse[i];
            }
        }
        mDebug.error("No position found!");
        return null;
 	}
 	private String getTeam(String s){
 	    String [] parse = s.split(" ");
 	    return parse[parse.length-1];
 	}
 	
 	private AUPlayer getPlayerFromList( Vector<AUPlayer> v, String firstName, String lastName ) {
 		for (int j = 0; j < v.size(); j++) {
 			AUPlayer p2 = v.elementAt(j);
 			// if the first name and last name match AND the player is not already elig
 			if (    firstName.equals(p2.getFirstName()) 	&&
 					lastName.equals(p2.getLastName())) {
 				return p2;
 			}
 		}
 		return null;
 	}
 	
 	private void checkForRookieBatters( Vector<AUBatter> v, Vector<AUPlayer> v2009 ) {
 		for (int i = 0; i < v.size(); i++) {
 			AUBatter p = v.elementAt(i);
 			
 			for (int j = 0; j < mBatterList.size(); j++) {
 				AUPlayer p2 = mBatterList.elementAt(j);
 				// if the first name and last name match AND the player is not already elig
 				if (    p.getFirstName().equals(p2.getFirstName()) 	&&
 						p.getLastName().equals(p2.getLastName())) {
 					// check the 2009 list
 					if (getPlayerFromList(v2009, p2.getFirstName(), p2.getLastName()) != null) {
 						p2.setRookie(true);
 					}
 				}
 			}
 		}
 	}
 	
 	private void checkForRookiePitchers( Vector<AUPitcher> v, Vector<AUPlayer> v2009 ) {
 		for (int i = 0; i < v.size(); i++) {
 			AUPitcher p = v.elementAt(i);
 			
 			for (int j = 0; j < mPitcherList.size(); j++) {
 				AUPlayer p2 = mPitcherList.elementAt(j);
 				// if the first name and last name match AND the player is not already elig
 				if (    p.getFirstName().equals(p2.getFirstName()) 	&&
 						p.getLastName().equals(p2.getLastName())) {
 					// check the 2009 list
 					if (getPlayerFromList(v2009, p2.getFirstName(), p2.getLastName()) != null) {
 						p2.setRookie(true);
 					}
 				}
 			}
 		}
 	}
 	
 	private void checkBatterEligibility( Vector<AUPlayer> v, String pos, int position ) {
 		// TODO: make this better, terribly inefficient
 		for (int i = 0; i < v.size(); i++ ) {
 			// get the next player in eligibility list
 			AUPlayer p = v.elementAt(i);
 			
 			// walk the batter list to see if we get a name match
 			for (int j = 0; j < mBatterList.size(); j++ ) {
 				AUPlayer p2 = mBatterList.elementAt(j);
 				// if the first name and last name match AND the player is not already elig
 				if (    p.getFirstName().equals(p2.getFirstName()) 	&&
 						p.getLastName().equals(p2.getLastName())) {
 					// NAME MATCH
 					if (!p2.isEligible(position)) {
 						// add the eligibility only if they are not already elig
 						
 						if (position == AUPlayer.AUPlayerPosition.OUTFIELD) {
 							// special case this
 						}
 						p2.addPosition(pos);
 					}
 					break;	// we found the name, break outta here
 				}
 			}
 		}
 	}
 	
 	private void checkPitcherEligibility( Vector<AUPlayer> v, String pos, int position ) {
 		// TODO: make this better, terribly inefficient
 		for (int i = 0; i < v.size(); i++ ) {
 			// get the next player in eligibility list
 			AUPlayer p = v.elementAt(i);
 			
 			// walk the batter list to see if we get a name match
 			for (int j = 0; j < mPitcherList.size(); j++ ) {
 				AUPlayer p2 = mPitcherList.elementAt(j);
 				// if the first name and last name match AND the player is not already elig
 				if (    p.getFirstName().equals(p2.getFirstName()) 	&&
 						p.getLastName().equals(p2.getLastName())) {
 					// NAME MATCH
 					if (!p2.isEligible(position)) {
 						// add the eligibility only if they are not already elig
 						p2.addPosition(pos);
 					}
 					break;	// we found the name, break outta here
 				}
 			}
 		}
 	}
 
 	private void outputBatterPosition( int position ) {
 		PlayerRater pr = PlayerRater.getInstance();
 		for(int i = 0; i < mBatterList.size(); i++) {
 			AUBatter b = mBatterList.get(i);
 			if (position==AUPlayer.AUPlayerPosition.OUTFIELD) {
 				if (mBatterList.get(i).isEligible(AUPlayer.AUPlayerPosition.OUTFIELD) ||
 						mBatterList.get(i).isEligible(AUPlayer.AUPlayerPosition.LEFTFIELD) ||
 						mBatterList.get(i).isEligible(AUPlayer.AUPlayerPosition.RIGHTFIELD) ||
 						mBatterList.get(i).isEligible(AUPlayer.AUPlayerPosition.CENTERFIELD) ){
 					b.writePlayer(pr.getBatterStream());
 				}
 			} else if (mBatterList.get(i).isEligible(position)) {
 				b.writePlayer(pr.getBatterStream());
 		    }
 	         
 		}
 	}
 	
 	private void outputPitcherPosition( int position ) {
 		PlayerRater pr = PlayerRater.getInstance();
 		for(int i = 0; i < mPitcherList.size(); i++) {
 			AUPitcher p = mPitcherList.get(i);
 			if (mPitcherList.get(i).isEligible(position)) {
 				p.writePlayer(pr.getPitcherStream());
 		    }
 		}
 	}
 	private void setOverallRanking( ) {
 		AUBatter.SORT_TYPE = AUBatter.SORT_AVG;
         Collections.sort(mBatterList);
         for(int i = 0; i < mBatterList.size(); i++) {
             mBatterList.get(i).setAvgRank(i);
         }
 		
         AUBatter.SORT_TYPE = AUBatter.SORT_HRS;
         Collections.sort(mBatterList);
         for(int i = 0; i < mBatterList.size(); i++) {
             mBatterList.get(i).setHrRank(i);
         }
 
         AUBatter.SORT_TYPE = AUBatter.SORT_OPS;
         Collections.sort(mBatterList);
         for(int i = 0; i < mBatterList.size(); i++) {
             mBatterList.get(i).setOpsRank(i);
         }
         
         AUBatter.SORT_TYPE = AUBatter.SORT_RBI;
         Collections.sort(mBatterList);
         for(int i = 0; i < mBatterList.size(); i++) {
             mBatterList.get(i).setRbiRank(i);
         }
         
         AUBatter.SORT_TYPE = AUBatter.SORT_RUNS;
         Collections.sort(mBatterList);
         for(int i = 0; i < mBatterList.size(); i++) {
             mBatterList.get(i).setRunRank(i);
         }
 
         AUBatter.SORT_TYPE = AUBatter.SORT_SB;
         Collections.sort(mBatterList);
         for(int i = 0; i < mBatterList.size(); i++) {
             mBatterList.get(i).setSbRank(i);
         }
 	}
 }
