 /**
  *  This file is part of MediaDaemon.
  *
  *  MediaDaemon is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  MediaDaemon is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with MediaDaemon.  If not, see <http://www.gnu.org/licenses/>.
  */
 package ws.ip4u.mediadaemon;
 
 import java.io.File;
 import java.io.IOException;
 import java.sql.SQLException;
 import java.util.List;
 import javax.xml.parsers.ParserConfigurationException;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.xml.sax.SAXException;
 import ws.ip4u.mediadaemon.Config.ConfigException;
 import ws.ip4u.mediadaemon.Config.ConfigTestingException;
 import ws.ip4u.mediadaemon.FileMover.FileMoveException;
 
 /**
  * TODO: turn this thing into a service somehow
  *
  * @author jalsk
  */
 public final class MediaDaemon
 {
 	private static Log log = LogFactory.getLog(MediaDaemon.class);
 
 	private MediaDaemon()
 	{
 	}
 
 	public static void main(String[] args)
 	{
 		try
 		{
 			Config config = new Config(args);
 
 			DB db = new DB(config.getDbPath());
 
 			Scanner sc = new Scanner(config.getShowPath(), config.getTorrentPath(), config.isPretend());
 			List<Series> shows = sc.getShows();
 
 			TVDB api = new TVDB(config.getApiKey());
 
 			for(Series show : shows)
 			{
 				api.updateSeriesInformation(show);
 			}
 
			db.updateAllSeries(shows);

 			FileMover fm = new FileMover(new File(config.getShowPath()));
 			for(Series show : shows)
 			{
 				fm.fixSeriesNaming(show);
 			}
 		}
 		//<editor-fold defaultstate="collapsed" desc="catch statements">
 		catch(ConfigException e)
 		{
 			log.fatal("There was a problem with the configuration specified.\n" + e.getMessage(), e);
 		}
 		catch(ConfigTestingException e)
 		{
 			log.info("Finished testing the config file. Exiting.");
 		}
 		catch(ParserConfigurationException e)
 		{
 			log.error("Problem parsing data from the episode information provider.\n" + e.getMessage(), e);
 		}
 		catch(SAXException e)
 		{
 			log.error("Problem parsing data from the episode information provider.\n" + e.getMessage(), e);
 		}
 		catch(IOException e)
 		{
 			log.error("Problem parsing data from the episode information provider.\n" + e.getMessage(), e);
 		}
 		catch(FileMoveException e)
 		{
 			log.error("Problem moving the files into their appropriate locations.\n" + e.getMessage(), e);
 		}
 		catch(SQLException e)
 		{
 			log.error("Problem interacting with the sqlite database.\n" + e.getMessage(), e);
 		}
 		//</editor-fold>
 	}
 }
