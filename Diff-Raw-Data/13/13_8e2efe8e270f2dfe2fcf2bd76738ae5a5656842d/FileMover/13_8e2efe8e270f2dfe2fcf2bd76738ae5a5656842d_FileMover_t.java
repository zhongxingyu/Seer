 package ws.ip4u.mediadaemon;
 
 import java.io.File;
 import java.io.IOException;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 /**
  *
  * @author jalsk
  */
 public class FileMover
 {
 	private File tvShowPath;
 	private Log log = LogFactory.getLog(FileMover.class);
 
 	public FileMover(File tvShowPath)
 	{
 		this.tvShowPath = tvShowPath;
 	}
 
 	public void moveAndRenameEpisode(Episode episode, File seasonPath) throws FileMoveException
 	{
 		if(!seasonPath.exists())
 		{
 			if(!episode.getFileOption().createDirs)
 			{
 				log.info("Would have created directory: " + seasonPath.getAbsolutePath());
 			}
 			else if(!seasonPath.mkdirs())
 			{
 				throw new FileMoveException("Error creating the directory: " + seasonPath);
 			}
 		}
 
 		File oldEpisode = new File(episode.getParentDir(), episode.getFilename());
 		File newEpisode = new File(seasonPath, episode.getFormattedName());
 		if(!oldEpisode.equals(newEpisode))
 		{
 			if(!episode.getFileOption().createDirs && !fixPermissions(oldEpisode))
 			{
 				throw new FileMoveException("Can't set the file as readable and writable.");
 			}
 			log.info("Moving file " + oldEpisode + " to " + newEpisode);
 			if(episode.getFileOption() == FileOption.NOTHING)
 			{
 				log.info("Would have moved file " + oldEpisode.getAbsolutePath() + " to " + newEpisode.getAbsolutePath());
 			}
 			else if(episode.getFileOption() == FileOption.MOVE && !oldEpisode.renameTo(newEpisode))
 			{
 				throw new FileMoveException("Error moving the requested file: " + episode.getParentDir() + " to: " + seasonPath);
 			}
 			else if(episode.getFileOption() == FileOption.COPY)
 			{
 				try
 				{
 					FileUtils.copyFile(oldEpisode, newEpisode);
 				}
 				catch(IOException e)
 				{
 					throw new FileMoveException("Error copying the requested file: f" + episode.getParentDir() + " to: " + seasonPath, e);
 				}
 			}
 		}
 	}
 
 	public void fixSeasonNaming(Season season, File seriesPath) throws FileMoveException
 	{
 		for(Episode e : season.getEpisodes())
 		{
 			moveAndRenameEpisode(e, new File(seriesPath, season.getSeasonName()));
 		}
 	}
 
 	public void fixSeriesNaming(Series series) throws FileMoveException
 	{
 		for(Season s : series.getSeasons())
 		{
 			fixSeasonNaming(s, new File(tvShowPath, series.getShowName()));
 		}
 	}
 
 	private boolean fixPermissions(File f)
 	{
 		boolean success = f.canWrite() && f.canRead();
 		if(!f.canWrite())
 		{
 			success = f.setWritable(true);
 		}
 		if(success && !f.canRead())
 		{
 			success = f.setReadable(true);
 		}
 		return success;
 	}
 
 	public class FileMoveException extends Exception
 	{
 		public FileMoveException(String message)
 		{
 			super(message);
 		}
 
 		public FileMoveException(String message, Exception e)
 		{
 			super(message, e);
 		}
 	}
 
 	/**
 	 * This enum refers to what we are supposed to do with a given file. If we want to move, copy, or leave a given file
 	 * alone.
 	 */
 	public enum FileOption
 	{
 		MOVE(true),
 		COPY(true),
 		NOTHING(false);
		public boolean createDirs;
 
 		FileOption(boolean createDirs)
 		{
 			this.createDirs = createDirs;
 		}
 	};
 }
