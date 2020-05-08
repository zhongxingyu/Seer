 package geopod.devices;
 
 import geopod.Geopod;
 import geopod.constants.ParticleImagePathConstants;
 import geopod.eventsystem.IObserver;
 import geopod.eventsystem.ISubject;
 import geopod.eventsystem.SubjectImpl;
 import geopod.eventsystem.events.GeopodEventId;
 import geopod.gui.panels.ImagePanel;
 import geopod.gui.panels.ParticleImagePanel;
 import geopod.utils.FileLoadingUtility;
 
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Random;
 
 import javax.swing.BorderFactory;
 import javax.swing.JPanel;
 import javax.swing.border.BevelBorder;
 
 import ucar.visad.quantities.CommonUnits;
 import visad.Real;
 import visad.VisADException;
 
 /**
  * The ParticleImager is a device that determines which particle formations, if
  * any, are present at a given {@link Geopod} current earth location.
  * 
  * @author Geopod Team
  * 
  */
 public class ParticleImager
 		implements ISubject
 {
 
 	/**
 	 * Relative Humidity is HIGH when 70% < RH <= 85% and defined as VERY_HIGH
 	 * when RH > 85%
 	 * 
 	 * @author Geopod Team
 	 * 
 	 */
 	enum RelativeHumidity
 	{
 		HIGH, VERY_HIGH,
 	}
 
 	private static final String NO_PARTICLE_FORMATION;
 	private static final String STELLAR_DENDRITES;
 	private static final String NEEDLES;
 	private static final String HOLLOW_COLUMNS;
 	private static final String SECTORED_PLATES;
 	private static final String DOUBLE_SPLIT_PLATES;
 	private static final String THIN_SMALL_PLATES;
 	private static final String LIQUID;
 
 	private static final String[][] CATEGORY_TABLE;
 
 	static
 	{
 		STELLAR_DENDRITES = "Stellar Dendrites";
 		NEEDLES = "Needles";
 		HOLLOW_COLUMNS = "Hollow Columns";
 		SECTORED_PLATES = "Sectored Plates";
 		DOUBLE_SPLIT_PLATES = "Double Split Plates";
 		THIN_SMALL_PLATES = "Thin Small Plates";
 		LIQUID = "Liquid";
 		NO_PARTICLE_FORMATION = "No image available";
 
 		// [Relative Humidity][ Temperature]
 		CATEGORY_TABLE = new String[][] { { THIN_SMALL_PLATES, HOLLOW_COLUMNS, THIN_SMALL_PLATES, THIN_SMALL_PLATES },
 				{ DOUBLE_SPLIT_PLATES, NEEDLES, SECTORED_PLATES, HOLLOW_COLUMNS } };
 	}
 
 	private ParticleImagePanel m_particlePanel;
 	private HashMap<String, List<BufferedImage>> m_imageMap;
 	private SubjectImpl m_subjectImpl;
 	private BufferedImage m_defaultImage;
 	private Geopod m_geopod;
 	private List<String> m_currentCategories;
 	private List<String> m_previousCategories;
 	private BufferedImage m_currentImage;
 	private String m_currentCategory;
 
 	/**
 	 * Constructs a ParticleImager with a current category of
 	 * NO_PARTICLE_FORMATION
 	 * 
 	 * @param geopod
 	 */
 	public ParticleImager (Geopod geopod)
 	{
 		m_geopod = geopod;
 		m_subjectImpl = new SubjectImpl ();
 
 		loadParticleImages ();
 
 		m_particlePanel = createParticlePanel ();
 		setDefaultImage ();
 
 		m_currentCategories = new ArrayList<String> ();
 		m_currentCategories.add (NO_PARTICLE_FORMATION);
 
 		m_previousCategories = new ArrayList<String> ();
 		m_previousCategories.add (NO_PARTICLE_FORMATION);
 
 		m_currentCategory = NO_PARTICLE_FORMATION;
 
 		m_currentImage = m_defaultImage;
 	}
 
 	/**
 	 * Creates an {@link ImagePanel} that serves as a view of the current state
 	 * of the ParticleImager
 	 * 
 	 * @return an image panel for displaying particle formations
 	 */
 	private ParticleImagePanel createParticlePanel ()
 	{
 		ParticleImagePanel particlePanel = new ParticleImagePanel ();
 		particlePanel.setBorder (BorderFactory.createBevelBorder (BevelBorder.RAISED));
 		particlePanel.addMouseListener (new MouseAdapter ()
 		{
 			@Override
 			public void mouseClicked (MouseEvent e)
 			{
 				//Select a new image to display such that new image != current image && 
 				//new image exists in the set of images specified by current categories
 				BufferedImage newImage = selectRandomImage (ParticleImager.this.m_currentCategories);
 				ParticleImager.this.m_particlePanel.setImage (newImage);
 				ParticleImager.this.m_currentImage = newImage;
 			}
 		});
 
 		return (particlePanel);
 	}
 
 	/**
 	 * This method is responsible for loading and mapping all particle images
 	 * and their associated categories.
 	 */
 	private void loadParticleImages ()
 	{
 		m_imageMap = new HashMap<String, List<BufferedImage>> ();
 
 		try
 		{
 			ArrayList<String> paths = new ArrayList<String> ();
 			/*
 			paths.add ("//Resources/Images/Particles/Dendrites/Stellar_Dendrites/Stellar00.jpg");
 			paths.add ("//Resources/Images/Particles/Dendrites/Stellar_Dendrites/Stellar01.jpg");
 			paths.add ("//Resources/Images/Particles/Dendrites/Stellar_Dendrites/Stellar02.jpg");
 			paths.add ("//Resources/Images/Particles/Dendrites/Fernlike_Stellar_Dendrite/Fern00.jpg");
 			paths.add ("//Resources/Images/Particles/Dendrites/Fernlike_Stellar_Dendrite/Fern01.jpg");
 			paths.add ("//Resources/Images/Particles/Dendrites/Fernlike_Stellar_Dendrite/Fern02.jpg");
 			*/
 			paths.addAll (ParticleImagePathConstants.DENDRITES);
 			
 			List<BufferedImage> stellarDendrites = FileLoadingUtility.loadBufferedImages (paths);
 			m_imageMap.put (STELLAR_DENDRITES, stellarDendrites);
 
 			paths.clear ();
 			/*
 			paths.add ("//Resources/Images/Particles/Needles/needle00.jpg");
 			paths.add ("//Resources/Images/Particles/Needles/needle01.jpg");
 			*/
 			paths.addAll (ParticleImagePathConstants.NEEDLES);
 			
 			List<BufferedImage> needles = FileLoadingUtility.loadBufferedImages (paths);
 			m_imageMap.put (NEEDLES, needles);
 
 			paths.clear ();
 			/*
 			paths.add ("//Resources/Images/Particles/Columns/Hollow_Columns/hc00.jpg");
 			paths.add ("//Resources/Images/Particles/Columns/Hollow_Columns/hc01.jpg");
 			paths.add ("//Resources/Images/Particles/Columns/Hollow_Columns/hc02.jpg");
 			paths.add ("//Resources/Images/Particles/Columns/Hollow_Columns/hc03.jpg");
 			*/
 			paths.addAll (ParticleImagePathConstants.HOLLOW_COLUMNS);
 
 			List<BufferedImage> hollowColumns = FileLoadingUtility.loadBufferedImages (paths);
 			m_imageMap.put (HOLLOW_COLUMNS, hollowColumns);
 
 			paths.clear ();
 			/*
 			paths.add ("//Resources/Images/Particles/Plates/Sectored_Plates/sectored00.jpg");
 			paths.add ("//Resources/Images/Particles/Plates/Sectored_Plates/sectored01.jpg");
 			paths.add ("//Resources/Images/Particles/Plates/Sectored_Plates/sectored02.jpg");
 			paths.add ("//Resources/Images/Particles/Plates/Sectored_Plates/sectored03.jpg");
 			paths.add ("//Resources/Images/Particles/Plates/Sectored_Plates/sectored04.jpg");
 			paths.add ("//Resources/Images/Particles/Plates/Sectored_Plates/sectored05.jpg");
 			paths.add ("//Resources/Images/Particles/Plates/Sectored_Plates/sectored06.jpg");
 			paths.add ("//Resources/Images/Particles/Plates/Sectored_Plates/sectored07.jpg");
 			*/
 			paths.addAll (ParticleImagePathConstants.SECTORED_PLATES);
 
 			List<BufferedImage> sectoredPlates = FileLoadingUtility.loadBufferedImages (paths);
 			m_imageMap.put (SECTORED_PLATES, sectoredPlates);
 
 			paths.clear ();
 			/*
 			paths.add ("//Resources/Images/Particles/Plates/Double_Split_Plates/double_plate00.jpg");
 			paths.add ("//Resources/Images/Particles/Plates/Double_Split_Plates/split_plate00.jpg");
 			*/
 			paths.addAll (ParticleImagePathConstants.DOUBLE_SPLIT_PLATES);
 
 			List<BufferedImage> doubleSplitPlates = FileLoadingUtility.loadBufferedImages (paths);
 			m_imageMap.put (DOUBLE_SPLIT_PLATES, doubleSplitPlates);
 
 			paths.clear ();
 			/*
 			paths.add ("//Resources/Images/Particles/Plates/Thin_Small_Plates/thin_plate00.jpg");
 			paths.add ("//Resources/Images/Particles/Plates/Thin_Small_Plates/thin_plate01.jpg");
 			*/
 			paths.addAll (ParticleImagePathConstants.THIN_SMALL_PLATES);
 
 			List<BufferedImage> thinSmallPlates = FileLoadingUtility.loadBufferedImages (paths);
 			m_imageMap.put (THIN_SMALL_PLATES, thinSmallPlates);
 
 			paths.clear ();
 			//paths.add ("//Resources/Images/Particles/Liquid/Liquid00.png");
 			paths.add (ParticleImagePathConstants.LIQUID);
 
 			List<BufferedImage> liquid = FileLoadingUtility.loadBufferedImages (paths);
 			m_imageMap.put (LIQUID, liquid);
 
 			//String path = "//Resources/Images/Particles/defaultImage.jpg";
 			String path = ParticleImagePathConstants.DEFAULT;
 			m_defaultImage = FileLoadingUtility.loadBufferedImage (path);
 			List<BufferedImage> defaultImages = new ArrayList<BufferedImage> ();
 			defaultImages.add (m_defaultImage);
 			m_imageMap.put (NO_PARTICLE_FORMATION, defaultImages);
 		}
 		catch (IOException e)
 		{
 			e.printStackTrace ();
 		}
 	}
 
 	/**
 	 * Updates this ParticleImager to reflect the most current particle
 	 * formation category (if any) based on the Geopod's current sensor values.
 	 */
 	public void updateParticleImage ()
 	{
 		if (isParticleImagerActive ())
 		{
 			//The Particle Imager is Active (visible)
 			m_previousCategories = m_currentCategories;
 
			Real temperature = m_geopod.getSensorValue ("Temperature @ isobaric");
			Real relativeHumidity = m_geopod.getSensorValue ("Relative_humidity @ isobaric");
 
 			// Check for GRIB2 names if the above values do not exist.
 			if (temperature == null)
 			{
				temperature = m_geopod.getSensorValue ("Temperature @ pressure");
 			}
 			if (relativeHumidity == null)
 			{
				relativeHumidity = m_geopod.getSensorValue ("Relative_humidity @ pressure");
 			}
 
 			m_currentCategories = determineCategories (temperature, relativeHumidity);
 			
 			if (m_currentCategories.isEmpty ())
 			{
 				// No particle formations were found
 				// Don't paint the category now
 				m_particlePanel.setCategoryLabelOpaque (false);
 				m_currentCategories.add (NO_PARTICLE_FORMATION);
 			}
 			else
 			{
 				m_particlePanel.setCategoryLabelOpaque (true);
 			}
 
 			// Update image & send notification iff the current category list differs from the previous category list
 			if (!m_previousCategories.equals (m_currentCategories))
 			{
 				BufferedImage newImage = selectRandomImage (m_currentCategories);
 				m_currentImage = newImage;
 				m_particlePanel.setImage (newImage);
 				this.notifyObservers (GeopodEventId.PARTICLE_IMAGED);
 			}
 		}
 	}
 
 	/**
 	 * Sets the image of this ParticleImager to the default image
 	 */
 	private void setDefaultImage ()
 	{
 		m_particlePanel.setImage (m_defaultImage);
 	}
 
 	/**
 	 * Returns the specified {@link Real} temperature as a double Celsius value.
 	 * 
 	 * @param temperature
 	 *            the temperature Real in Kelvin
 	 * @return a double value in Celsius; null if temperature is null or missing
 	 */
 	private double convertTemperatureValue (Real temperature)
 	{
 		double temperatureCelcius = Double.NaN;
 
 		if (temperature == null || temperature.isMissing ())
 		{
 			return (temperatureCelcius);
 		}
 
 		try
 		{
 			temperatureCelcius = temperature.getValue (CommonUnits.CELSIUS);
 		}
 		catch (VisADException e)
 		{
 			e.printStackTrace ();
 		}
 
 		return (temperatureCelcius);
 	}
 
 	/**
 	 * Returns a double representation of the specified {@Real} relative
 	 * humidity value.
 	 * 
 	 * @param relativeHumidity
 	 *            the relative humidity value
 	 * @return a double representation of relative humidity; null if
 	 *         relativeHumidity is null or missing
 	 */
 	private double convertRelativeHumidityValue (Real relativeHumidity)
 	{
 		double relativeHumidityPercent = Double.NaN;
 
 		// If the relativeHumidity is missing, return NaN as an indicator value.
 		if (relativeHumidity == null || relativeHumidity.isMissing ())
 		{
 			return (relativeHumidityPercent);
 		}
 
 		try
 		{
 			relativeHumidityPercent = relativeHumidity.getValue (CommonUnits.PERCENT);
 		}
 		catch (VisADException e)
 		{
 			e.printStackTrace ();
 		}
 
 		return (relativeHumidityPercent);
 	}
 
 	/**
 	 * This method returns a list of particle categories given the specified
 	 * temperature and relative humidity values
 	 * 
 	 * @param temperature
 	 * @param relativeHumidity
 	 * @return a list of particle formation categories; an empty list indicates
 	 *         no particle formations were found
 	 */
 	private List<String> determineCategories (Real temperature, Real relativeHumidity)
 	{
 		// Logic to determine which image to use
 		List<String> categories = new ArrayList<String> ();
 
 		// Get the actual values, may return NaN if null or missing or fails to convert
 		double temperatureValue = convertTemperatureValue (temperature);
 		double relativeHumidityValue = convertRelativeHumidityValue (relativeHumidity);
 
 		if (Double.isNaN (temperatureValue))
 		{
 			// Return empty list since temperature does not exist
 			return (categories);
 		}
 
 		if (Double.isNaN (relativeHumidityValue))
 		{
 			// Return empty list since relative humidity does not exist
 			return (categories);
 		}
 
 		if (relativeHumidityValue >= 70)
 		{
 			determineCategories (temperatureValue, relativeHumidityValue, categories);
 		}
 
 		return (categories);
 	}
 
 	/**
 	 * This is a helper method to determine particle categories.
 	 * 
 	 * @param temperatureValue
 	 * @param relativeHumidityValue
 	 * @param categories
 	 */
 	private void determineCategories (double temperatureValue, double relativeHumidityValue, List<String> categories)
 	{
 		RelativeHumidity relativeHumidityCategory = RelativeHumidity.HIGH;
 
 		if (relativeHumidityValue >= 85)
 		{
 			relativeHumidityCategory = RelativeHumidity.VERY_HIGH;
 
 			if (temperatureValue > 0)
 			{
 				categories.add (LIQUID);
 			}
 
 			// Special subrange
 			if (temperatureValue > -17 && temperatureValue <= -13)
 			{
 				// RH > 85 and -17C < T < -13C
 				categories.add (STELLAR_DENDRITES);
 			}
 		}
 
 		double[] temperatureBounds = { -22, -10, -3, 0 };
 
 		int rhIndex = relativeHumidityCategory.ordinal ();
 
 		for (int range = 0; range < temperatureBounds.length; ++range)
 		{
 			if (temperatureValue <= temperatureBounds[range])
 			{
 				String category = CATEGORY_TABLE[rhIndex][range];
 				categories.add (category);
 				break;
 			}
 		}
 	}
 
 	/**
 	 * This method selects a random particle image to use given the specified
 	 * particle categories. Please note this method has the side effect of
 	 * setting the current particle category for this ParticleImager. This side
 	 * effect is necessary because we do not know what the final particle
 	 * category will be until this method finishes.
 	 * 
 	 * @param categories
 	 *            a list of particle formation categories
 	 * @return an image representing the randomly selected particle formation
 	 */
 	private BufferedImage selectRandomImage (List<String> categories)
 	{
 		BufferedImage randomImage = getRandomImage (categories);
 
 		if (m_imageMap.get (m_currentCategory).size () == 1)
 		{
 			return (randomImage);
 		}
 
 		while (randomImage == m_currentImage)
 		{
 			randomImage = getRandomImage (categories);
 		}
 		return (randomImage);
 	}
 
 	private BufferedImage getRandomImage (List<String> categories)
 	{
 		Random r = new Random ();
 
 		int categoryIndex = r.nextInt (categories.size ());
 		String category = categories.get (categoryIndex);
 
 		m_currentCategory = category;
 
 		int imageIndex = r.nextInt (m_imageMap.get (category).size ());
 		BufferedImage image = m_imageMap.get (category).get (imageIndex);
 		m_particlePanel.setImageCategory (category);
 		m_particlePanel.repaintCategory ();
 
 		return (image);
 	}
 
 	/**
 	 * Returns a JPanel that represents one view of this ParticleImager
 	 * 
 	 * @return a panel containing a graphical representation of particles
 	 */
 	public JPanel getParticlePanel ()
 	{
 		return (m_particlePanel);
 	}
 
 	/**
 	 * Returns the most recent particle formation category associated with this
 	 * ParticleImager
 	 * 
 	 * @return a name that represents a particle formation category
 	 */
 	public String getCurrentCategory ()
 	{
 		return (m_currentCategories.get (0));
 	}
 
 	/**
 	 * This method will activate this ParticleImager, update the particle
 	 * formation category, and notify any observers that it has been opened.
 	 */
 	public void openParticleImager ()
 	{
 		m_particlePanel.setVisible (true);
 		this.notifyObservers (GeopodEventId.PARTICLE_IMAGER_OPENED);
 		this.updateParticleImage ();
 	}
 
 	/**
 	 * This method deactivates the ParticleImager and notifies any observers
 	 * that it has been closed.
 	 */
 	public void closeParticleImager ()
 	{
 		m_particlePanel.setVisible (false);
 		this.notifyObservers (GeopodEventId.PARTICLE_IMAGER_CLOSED);
 	}
 
 	/**
 	 * This is a convenience method for toggling the state of the ParticleImager
 	 */
 	public void toggleParticleImager ()
 	{
 		boolean isVisible = m_particlePanel.isVisible ();
 
 		if (isVisible)
 		{
 			closeParticleImager ();
 		}
 		else
 		{
 			openParticleImager ();
 		}
 	}
 
 	/**
 	 * Returns whether or not this ParticleImager is active
 	 * 
 	 * @return true if so; false otherwise
 	 */
 	public boolean isParticleImagerActive ()
 	{
 		return (m_particlePanel.isVisible ());
 	}
 
 	@Override
 	public void addObserver (IObserver observer, GeopodEventId eventId)
 	{
 		m_subjectImpl.addObserver (observer, eventId);
 	}
 
 	@Override
 	public void removeObserver (IObserver observer, GeopodEventId eventId)
 	{
 		m_subjectImpl.removeObserver (observer, eventId);
 	}
 
 	@Override
 	public void notifyObservers (GeopodEventId eventId)
 	{
 		m_subjectImpl.notifyObservers (eventId);
 	}
 
 	@Override
 	public void removeObservers ()
 	{
 		m_subjectImpl.removeObservers ();
 	}
 }
