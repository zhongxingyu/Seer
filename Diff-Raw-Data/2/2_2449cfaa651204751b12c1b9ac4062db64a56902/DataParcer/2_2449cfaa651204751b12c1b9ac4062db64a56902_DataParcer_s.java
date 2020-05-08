 package com.argando.parcersample;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.htmlcleaner.HtmlCleaner;
 import org.htmlcleaner.TagNode;
 
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentTransaction;
 import android.util.Log;
 
 public class DataParcer
 {
 	private static final String	LOG						= "DataParcer";
 
 	private static final String	mSiteFootballTable		= "http://www.football.ua/scoreboard/";
 	private static final String	mSiteFootballSopcast	= "http://www.livefootball.ws/";
 	private static URL			mSiteUrl;
 	private HtmlCleaner			mHtmlHelper;
 	private TagNode				mRootElement;
 
 	private void setUrl(String url)
 	{
 		try
 		{
 			mSiteUrl = new URL(url);
 		}
 		catch (MalformedURLException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			Log.w(LOG, "there is a problem with mSiteULR = " + mSiteFootballTable);
 		}
 	}
 
 	public DataParcer()
 	{
 		mHtmlHelper = new HtmlCleaner();
 		setUrl(mSiteFootballTable);
 	}
 
 	public List<League> parceScoreboard()
 	{
 		getRootElement();
 		TagNode scoreTable = getScoreTable();
 		TagNode[] leaguesData = getLeagueData(scoreTable);
 		List<League> legues = getLeagues(leaguesData);
 		initLiveFootballMainPage(legues);
 		return legues;
 	}
 
 	private void initLiveFootballMainPage(List<League> legues)
 	{
 		setUrl(mSiteFootballSopcast);
 		getRootElement();
 		TagNode[] mainSop = mRootElement.getElementsByAttValue(HtmlHelper.ID, HtmlHelper.MAIN_SOP, true, false);
 		TagNode[] sopElement = mainSop[0].getElementsByAttValue(HtmlHelper.CLASS, "base custom", true, false);
 		for (int i = 0; i < legues.size(); i++)
 		{
 			for (int j = 0; j < legues.get(i).getSize(); j++)
 			{
 				if (legues.get(i).getMatch(j).isOnlineStatus() == 1)
 				{
 					String team1 = legues.get(i).getMatch(j).getFirstTeam();
 					String team2 = legues.get(i).getMatch(j).getSecondTeam();
 					legues.get(i).getMatch(j).linkToSopcast = findMatchesForSopcast(team1, team2, sopElement);
 				}
 			}
 		}
 	}
 
 	private String findMatchesForSopcast(String team1, String team2, TagNode[] sopElement)
 	{
 		TagNode[] element;
 		for (int i = 0; i < sopElement.length; i++)
 		{
 			element = sopElement[i].getElementsByName(HtmlHelper.DIV, false);
 			String name = element[0].getText().toString().trim();
			if (name.contains(team1) || name.contains(team2))
 			{
 				TagNode[] linkElement = sopElement[i].getElementsByAttValue(HtmlHelper.CLASS, "argr_custom more", true, false);
 
 				for (TagNode aTag : sopElement[i].getElementsByName("a", true))
 				{
 					String link = aTag.getAttributeByName("href");
 					if (link != null && link.length() > 0)
 						return openSopcastLink(link);
 				}
 			}
 		}
 		return "";
 	}
 
 	private String openSopcastLink(String link)
 	{
 		setUrl(link);
 		getRootElement();
 
 		for (TagNode aTag : mRootElement.getElementsByName("a", true))
 		{
 			String sopLink = aTag.getAttributeByName("href");
 			if (sopLink != null && sopLink.length() > 0 && sopLink.contains("sop://broker.sopcast.com:"))
 				return sopLink;
 		}
 		return "";
 	}
 
 	private boolean getRootElement()
 	{
 		try
 		{
 			mRootElement = mHtmlHelper.clean(mSiteUrl);
 		}
 		catch (IOException e)
 		{
 			e.printStackTrace();
 		}
 		return false;
 	}
 
 	private TagNode getScoreTable()
 	{
 		TagNode[] scoreTable = mRootElement.getElementsByAttValue(HtmlHelper.CLASS, HtmlHelper.BCC, true, false);
 		return scoreTable[0];
 	}
 
 	private TagNode[] getLeagueData(TagNode scoreTable)
 	{
 		TagNode[] rootElementForLeague = scoreTable.getElementsByName(HtmlHelper.DIV, false);
 		return rootElementForLeague;
 	}
 
 	private List<League> getLeagues(TagNode[] leaguesData)
 	{
 		List<League> leagues = new ArrayList<League>();
 		League newLeague = null;
 		Match newMatch;
 
 		for (int i = 0; i < leaguesData.length; i++)
 		{
 			if (leaguesData[i].getAttributeByName(HtmlHelper.CLASS).equals(HtmlHelper.BLINE))
 			{
 				if (leaguesData[i].getElementsByName(HtmlHelper.A, false).length == 0)
 				{
 					newLeague = new League(leaguesData[i].getElementsByName(HtmlHelper.H1, true)[0].getText().toString().trim());
 				}
 				else
 				{
 					newLeague = new League(leaguesData[i].getElementsByName(HtmlHelper.A, true)[0].getText().toString().trim());
 				}
 				leagues.add(newLeague);
 				Log.w(LOG, "add legue " + newLeague.getName());
 			}
 			else if (leaguesData[i].getAttributeByName(HtmlHelper.CLASS).equals(HtmlHelper.TABLOLINE1))
 			{
 				String date = "";
 				String team1 = "";
 				String team2 = "";
 				String score1 = "";
 				String score2 = "";
 				int isonline = 0;
 				TagNode[] dataDate = leaguesData[i].getElementsByAttValue(HtmlHelper.CLASS, HtmlHelper.TABLODATE, true, false);
 				if (dataDate[0] != null)
 				{
 					date = dataDate[0].getText().toString().trim();
 				}
 
 				TagNode[] dataTeam1 = leaguesData[i].getElementsByAttValue(HtmlHelper.CLASS, HtmlHelper.TABLOTEAM1, true, false);
 				if (dataTeam1[0] != null)
 				{
 					team1 = dataTeam1[0].getText().toString().trim();
 				}
 
 				TagNode[] dataTeam2 = leaguesData[i].getElementsByAttValue(HtmlHelper.CLASS, HtmlHelper.TABLOTEAM2, true, false);
 				if (dataTeam2[0] != null)
 				{
 					team2 = dataTeam2[0].getText().toString().trim();
 				}
 
 				TagNode[] scoreData = leaguesData[i].getElementsByAttValue(HtmlHelper.CLASS, HtmlHelper.TABLOC, true, false);
 
 				String scoreLink = " ";
 				if (scoreData[0] != null)
 				{
 					// Need refactoring
 					TagNode score[] = leaguesData[i].getElementsByAttValue(HtmlHelper.CLASS, HtmlHelper.TABLOGSCORE, true, false);
 
 					for (TagNode aTag : scoreData[0].getElementsByName("a", true))
 					{
 						scoreLink = "  ";
 						String link = aTag.getAttributeByName("href");
 						if (link != null && link.length() > 0)
 							scoreLink = link;
 					}
 
 					isonline = 2;
 					if (score.length < 1)
 					{
 						score = leaguesData[i].getElementsByAttValue(HtmlHelper.CLASS, HtmlHelper.TABLORSCORE, true, false);
 						isonline = 1;
 					}
 					if (score.length < 1)
 					{
 						score = leaguesData[i].getElementsByAttValue(HtmlHelper.CLASS, HtmlHelper.TABLOGRAYSCORE, true, false);
 						isonline = 0;
 					}
 
 					if (score.length < 1)
 						Log.w(LOG, "can't find score for match" + team1 + " - " + team2 + "league = " + newLeague.getName());
 					if (score[0] != null && score[1] != null)
 					{
 						score1 = score[0].getText().toString().trim();
 						score2 = score[1].getText().toString().trim();
 					}
 				}
 				if (!date.isEmpty() && !team1.isEmpty() && !team2.isEmpty() && !score1.isEmpty() && !score2.isEmpty())
 				{
 					newMatch = new Match(date, team1, team2, score1, score2, newLeague.getName(), isonline, scoreLink);
 					if (newLeague != null)
 					{
 						newLeague.addMatch(newMatch);
 						Log.w(LOG, "match added" + team1 + " - " + team2 + "league = " + newLeague.getName() + isonline);
 					}
 				}
 			}
 		}
 		return leagues;
 	}
 
 	public void getDataForMatch(int id)
 	{
 		TagNode[] scoreTable = mRootElement.getElementsByAttValue(HtmlHelper.CLASS, "wblock", true, false);
 	}
 }
