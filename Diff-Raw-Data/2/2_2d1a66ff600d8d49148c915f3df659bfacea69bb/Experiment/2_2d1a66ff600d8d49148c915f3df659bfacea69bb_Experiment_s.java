 /**
  * 
  */
 package edu.vanderbilt.psychology.model;
 
 import java.io.FileWriter;
 import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
 
 import com.thoughtworks.xstream.XStream;
 
 import edu.vanderbilt.psychology.model.elements.ImageElementModel;
 import edu.vanderbilt.psychology.model.elements.TextModelElement;
 import edu.vanderbilt.psychology.model.properties.Appearance;
 import edu.vanderbilt.psychology.model.properties.DataSource;
 import edu.vanderbilt.psychology.model.properties.Movement;
 import edu.vanderbilt.psychology.model.properties.Position;
 
 /**
  * <p>
  * An {@link Experiment} is a storage container for all data involved with
  * saving an experiment. This class is serialized and deserialized in order to
  * create an Experiment Builder save file. The {@link Experiment} includes all
  * {@link Slide}s that are used in the experiment, and may (TODO) eventually
  * also contain any plugin code required for an experiment to be executed by the
  * player.
  * </p>
  * 
  * <p>
  * The {@link Experiment} class can be used inside many of code bases. Mainly,
  * it will be used inside both of the builder component, and the player
  * component. Therefore, any code used in just one component of EB should be
  * kept out of the {@link Experiment}.
  * </p>
  * 
  * <h3>Details</h3> The {@link Experiment} object is only read from or written
  * to at specific times. Specifically,
  * <ul>
  * <li>When opening a saved experiment file</li>
  * <li>When switching slides</li>
  * <li>Right before writing an experiment file to disk</li>
  * </ul>
  * 
  * <p>
  * Specifically, when loading an experiment from file, the file is deserialized
  * into an instance of {@link Experiment}. The GUI is then configured to load
  * that {@link Experiment} as it's data, and then control is handed to the GUI.
  * </p>
  * 
  * <p>
  * When switching slides, the current GUI data is packed into the current
  * {@link Slide} and the next {@link Slide} is opened. If there is no next
  * {@link Slide} yet, then a new default {@link Slide} is created and displayed
  * </p>
  * 
  * <p>
  * When a save operation is requested, all required data is packed into an
  * {@link Experiment} and then the {@link Experiment} is serialized to disk.
  * </p>
  * 
  * <h3>Performance Notes</h3>
  * <p>
  * At the time of this writing the speed of saving an {@link Experiment} to disk
  * is not a priority requirement. An {@link Experiment} is simply serialized to
  * XML using the {@link XStream} package.
  * </p>
  * 
  * @author Hamilton Turner
  * 
  */
 public class Experiment {
 	private Slide[] slides_ = new Slide[5];
 
 	// TODO add some cool checks in here to ensure that if we are saving over a
 	// slide, then that is the same slide we are already pointing to. There
 	// should very very infrequently be a case where we save a completely new
 	// slide over an old one. Normally we will just modify the old one
 	public void saveSlide(Slide s, int position) {
 		if (position < 0)
 			throw new IllegalArgumentException("position must be positive");
 
 		if (position > (slides_.length - 1)) {
 			Slide[] bigger = new Slide[position + 1];
 			int i = 0;
 			for (Slide oldSlide : slides_)
 				bigger[i++] = oldSlide;
 
 			slides_ = bigger;
 		}
 
 		slides_[position] = s;
 		System.out.println("Saved a slide!");
 	}
 
 	/**
 	 * Gets a {@link Slide} from the {@link Experiment}. Returns a default slide
 	 * if one does not exist
 	 * 
 	 * @param position
 	 * @return
 	 */
 	public Slide getSlide(int position) {
 		if (position < 0)
 			throw new IllegalArgumentException("position must be positive");
 
 		if ((position > (slides_.length - 1)) || (slides_[position] == null)) {
 			System.out.println("No slide at position " + position);
 			System.out.println("Experiment size: " + getSize());
 			System.out.println("Returning new slide");
 
 			return new Slide();
 		}
 
 		return slides_[position];
 	}
 
 	/**
 	 * Gets the number of slides in the {@link Experiment}
 	 * 
 	 * @return
 	 */
 	public int getSize() {
 		return slides_.length;
 	}
 
 	public void saveExperiment() {
 		XStream xs = new XStream();
 
 		xs.alias("Experiment", Experiment.class);
 		xs.alias("Slide", Slide.class);
 		xs.alias("ImageElement", ImageElementModel.class);
 		xs.alias("TextElement", TextModelElement.class);
 		xs.alias("DataSource", DataSource.class);
 		xs.alias("Appearance", Appearance.class);
 		xs.alias("Position", Position.class);
 		xs.alias("Movement", Movement.class);
 
 		try {
 			FileWriter fw = new FileWriter("test.xml");
 			fw.write(xs.toXML(this));
 			fw.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		System.out.println("Exported!");
 		System.exit(0);
 	}
 }
