 package org.bardes.state;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FilenameFilter;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Marshaller;
 import javax.xml.bind.Unmarshaller;
 
 import org.bardes.entities.Cue;
 import org.bardes.entities.Show;
 import org.bardes.entities.Slide;
 import org.bardes.entities.Slide.Type;
 
 public class DBXML extends DB
 {
 	private File cuesDir;
 	private JAXBContext jaxbContext;
 	private Marshaller marshall;
 	private Unmarshaller unmarshall;
 
 	public DBXML() throws JAXBException
 	{
 		cuesDir = new File(baseDirectory);
 		cuesDir = new File(cuesDir, "cues");
 		if (!cuesDir.exists())
 			cuesDir.mkdirs();
 		
 		jaxbContext = JAXBContext.newInstance(Show.class, Cue.class);
 		marshall = jaxbContext.createMarshaller();
 		unmarshall = jaxbContext.createUnmarshaller();
 	}
 
 	@Override
 	public void close()
 	{
 	}
 
 	@Override
 	public List<Cue> getCues()
 	{
 		
 		File[] files = cuesDir.listFiles(new FilenameFilter()
 		{
 			@Override
 			public boolean accept(File arg0, String arg1)
 			{
 				return arg1.contains(".xml");
 			}
 		});
 		
 		SortedSet<Cue> list = new TreeSet<Cue>();
 		for (File f : files)
 		{
 			try
 			{
 				FileInputStream is = new FileInputStream(f);
 				try
 				{
 					Cue cue = (Cue) unmarshall.unmarshal(f);
 					list.add(cue);
 				}
 				finally
 				{
 					is.close();
 				}
 			}
 			catch (Exception e)
 			{
 				e.printStackTrace();
 			}
 		}
 		
 		return new ArrayList<Cue>(list);
 	}
 
 	@Override
 	public void save(Cue c)
 	{
 		File f = fileForCue(c.getCue());
 		persist(c,f);
 	}
 	
 	private void persist(Object obj, File dest)
 	{
 		try
 		{
 			marshall.marshal(obj, dest);
 		}
 		catch (JAXBException e)
 		{
 			throw new RuntimeException(e);
 		}
 	}
 
 	private File fileForCue(Double cue)
 	{
 		return new File(cuesDir, cue+".xml");
 	}
 
 	@Override
 	public void save(Show show)
 	{
 		File f = new File(baseDirectory, "show.xml");
 		persist(show, f);
 	}
 
 	@Override
 	public void save(Collection<Cue> cues)
 	{
 		for (Cue c : cues)
 		{
 			save(c);
 		}
 	}
 
 	@Override
 	public Show getShow()
 	{
 		File f = new File(baseDirectory, "show.xml");
 		if (!f.exists())
 		{
 			Show s = new Show();
 			s.setMaxProjectors(3);
 			s.setUploadDir("/Users/eric/mp/images");
 			save(s);
 			return s;
 		}
 		
 		try
 		{
 			Show show = (Show) unmarshall.unmarshal(f);
 			return show;
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	@Override
 	public Cue getCue(Double cueNum)
 	{
 		File f = fileForCue(cueNum);
 		try
 		{
 			Cue cue = (Cue) unmarshall.unmarshal(f);
 			return cue;
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	@Override
 	public void saveImage(Double cueNum, int displayNum, String fileName, Type type, Double fadeTime)
 	{
		Cue cue = loadCue(cueNum);
		
 		cue.setFadeTime(fadeTime);
 		
 		Slide slide = new Slide();
 		slide.setContentFile(fileName);
 		slide.setContentType(type);
 		cue.setSlide(displayNum, slide);
 		
 		save(cue);
 	}
 
	private Cue loadCue(Double cueNum)
	{
		Cue cue = null;
		for (Cue c : getCues())
		{
			if (c.getCue().equals(cueNum))
				cue = c;
		}
		if (cue == null)
		{
			cue = new Cue();
			cue.setCue(cueNum);
		}
		return cue;
	}

 	@Override
 	public void blankSlide(Double cueNum, int projector)
 	{
		Cue cue = loadCue(cueNum);
 		Slide slide = new Slide();
 		slide.setContentFile(null);
 		slide.setContentType(Type.BLANK);
 		cue.setSlide(projector, slide);
 		
 		save(cue);
 	}
 
 	@Override
 	public void trackSlide(Double cueNum, int projector)
 	{
		Cue cue = loadCue(cueNum);
 		Slide slide = new Slide();
 		slide.setContentFile(null);
 		slide.setContentType(Type.TRACKED);
 		cue.setSlide(projector, slide);
 		
 		save(cue);
 	}
 
 	@Override
 	public void deleteCue(Double cueNum)
 	{
 		File f = fileForCue(cueNum);
 		f.delete();
 	}
 
 }
