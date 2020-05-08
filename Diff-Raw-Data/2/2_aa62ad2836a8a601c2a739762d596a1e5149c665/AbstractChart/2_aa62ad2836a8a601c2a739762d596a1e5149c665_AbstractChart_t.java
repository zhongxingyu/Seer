 package googlechartwrapper;
 
 import googlechartwrapper.util.ArrayUtils;
 import googlechartwrapper.util.GenericAppender;
 import googlechartwrapper.util.IExtendedFeatureAppender;
 import googlechartwrapper.util.IFeatureAppender;
 
 import java.awt.Dimension;
 import java.lang.reflect.Field;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Queue;
 
 /**
  * The AbstractChart is the basechart for most charts and supports some basic
  * features as the dimension. Additionally the url generating part is done
  * by this class by using a reflection mechanism in {@link #collectUrlElements()} and 
  * {@link #collectUrlElements(List)}. Each implementing subclass shall use
  * {@link IExtendedFeatureAppender} to collect the chart parameters. These appenders 
  * are gathered by the reflection mechanism by default and added to the url, separated
  * by the default {@value #AMPERSAND_SEPARATOR}.
  * 
  * @author steffan
  * @author martin
  */
 public abstract class AbstractChart implements Chart {
 
 	/**
 	 * Default Chart API's location of the google service.
 	 */
 	public static final String GOOGLE_API = "http://chart.apis.google.com/chart?";
 	
 	/**
 	 * default separator for parameters.
 	 */
 	public static final String AMPERSAND_SEPARATOR = "&";
 	protected Queue<String> urlElements = new LinkedList<String>();
 	
 	protected Dimension chartDimension;
 	// private String newLine = System.getProperty("line.separator");
 	//	protected String values;
 	//protected Color[] dataColors;
 
 	/**
 	 * Generates an AbstractChart with the given chartDimension.
 	 * @param chartDimension size of the chart in pixel
 	 * @throws IllegalArgumentException if chartDimension == null
 	 */
 	public AbstractChart(Dimension chartDimension) {
 		
 		if(chartDimension == null)
 			throw new IllegalArgumentException("chartDimension can not be null");
 		this.chartDimension = chartDimension;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see googlechartwrapper.Chart#getUrl()
 	 */
 	public String getUrl(){
 		collectUrlElements(getAllAppenders());
 		return generateUrlString(GOOGLE_API);
 	}
 	
 	/**
 	 * Returns the generated chart url with the given Chart API's location. 
 	 * The base URL is simply added ahead of the generated chart parameters
 	 * by the {@link IExtendedFeatureAppender}. 
 	 * <p>The following format is used:
 	 * &lt;base URL&gt;&lt;other url parameters&gt;. <br>
 	 * For example: 
 	 * http://chart.apis.google.com/chart?cht=s&chs=300x300&chd=e:DICW|BkHC|DIEs <br>
 	 * where http://chart.apis.google.com/chart? is the apiLocation.
 	 * @param apiLocation the Chart API's location 
 	 * @return generated chart string
 	 * @see #GOOGLE_API
 	 */
 	public String getUrl (String apiLocation){
 		collectUrlElements(getAllAppenders());
 		return generateUrlString(apiLocation);
 	}
 
 	/*
 	 * { collectUrlElements();
 	 * 
 	 * return generateUrlString(); }
 	 */
 	
 	/**
 	 * Returns the chart type which is appended to the URL. 
 	 * @return representing string for a chart type  
 	 */
 	protected abstract String getUrlChartType();
 
 	/**
 	 * Returns the chart type of the chart.
 	 * @return chart type of the chart.
 	 */
 	protected abstract ChartType getChartType();
 	
 	/**
 	 * Returns all appenders from the extending class, but not inherited fields. It
 	 * requires that these appenders implement {@link IExtendedFeatureAppender} and
 	 * the fields are public or protected. If subclass fields are necessary this method
 	 * must be overwritten. 
 	 * 
 	 * It's recommended that this method is overwritten as it uses reflection which may
 	 * not be safe in all environments.
 	 * @return list of all appenders
 	 */ 
 	protected List<IExtendedFeatureAppender> getAllAppenders(){
 		List<IExtendedFeatureAppender> allExtendedFeatureAppenders = 
 			new ArrayList<IExtendedFeatureAppender>(5); 
 		
 		List<Field> fields = new ArrayList<Field>(); //every field (appenders)
 		fields.addAll(Arrays.asList(this.getClass().getDeclaredFields()));
 		Class current = this.getClass().getSuperclass(); //to deal with inheritance
		while (current.getSuperclass()!= null){
 			fields.addAll(Arrays.asList(current.getDeclaredFields()));
 			current = current.getSuperclass();
 		}
 		//Field[] fields = this.getClass().getDeclaredFields(); //alle Felder
 		
 		for (Field f: fields){
 			if (ArrayUtils.linearSearch(f.getType().getInterfaces(), IExtendedFeatureAppender.class)>=0){
 				//if field implements the IExtendedFeatureAppender - so e.g. a genericAppender
 				try { 
 					
 					allExtendedFeatureAppenders.add((IExtendedFeatureAppender)f.get(this));
 					//der Liste hinzufügen, und zwar das feld aus der aktuellen instanz					
 				} 
 				catch (IllegalArgumentException e) {
 					throw new RuntimeException(e); //todo mva: think about this!
 				} 
 				catch (IllegalAccessException e) {
 					throw new RuntimeException(e);
 				}
 			}
 		}
 		return allExtendedFeatureAppenders;
 	}
 
 	/**
 	 * Collects all base url elements: chart type and chart dimension.
 	 */
 	protected void collectUrlElements() {
 		urlElements.clear();
 		urlElements.offer(MessageFormat.format("cht={0}", this
 				.getUrlChartType()));
 		urlElements.offer(MessageFormat.format("chs={0}x{1}",
 				this.chartDimension.width, this.chartDimension.height));
 		/*if (values != null) {
 			urlElements.offer(this.values);
 		}
 		// converts the color objects into an hex equivalent for google
 		if (dataColors != null && dataColors.length > 0) {
 			StringBuffer bf = new StringBuffer(dataColors.length * 8 + 5);
 			bf.append("chco=");
 			for (Color c : dataColors) {
 				if (c.getAlpha()==255){
 					bf.append(MiscUtils.getSixCharacterHexValue(c));
 				}
 				else {
 					bf.append(MiscUtils.getEightCharacterHexValue(c));
 				}
 				
 				bf.append(",");
 			}
 			urlElements.offer(bf.toString().substring(0,
 					bf.toString().length() - 1));
 		}*/
 	}
 
 	protected void collectUrlElements(List<IExtendedFeatureAppender> appenders) {
 		collectUrlElements(); //alle Grundelemente laden
 		Map<String, FeatureAppender<IExtendedFeatureAppender>> m = 
 			new HashMap<String, FeatureAppender<IExtendedFeatureAppender>>();
 		//map fuer key=featureprefixstring (z.b. chm) 
 		//value=Appender für alle von diesem Typen
 		
 		for (IExtendedFeatureAppender ap : appenders) {
 			if (m.containsKey(ap.getFeaturePrefix())) { //wenn schon appender vorhanden
 				m.get(ap.getFeaturePrefix()).add(ap); //einfach hinzufügen
 			} else { 
 				//ansonsten muss neuer appender für diesen feature typ angelegt werden
 				FeatureAppender<IExtendedFeatureAppender> fa = new FeatureAppender<IExtendedFeatureAppender>(
 						ap.getFeaturePrefix());
 				fa.add(ap);
 				m.put(ap.getFeaturePrefix(), fa);
 			}
 		}
 		
 		List<FeatureAppender<IExtendedFeatureAppender>> values = 
 			new ArrayList<FeatureAppender<IExtendedFeatureAppender>>(m.values());
 		
 		Collections.sort(values, new Comparator<IExtendedFeatureAppender>(){
 			public int compare(IExtendedFeatureAppender arg0, 
 					IExtendedFeatureAppender arg1) {
 				return arg0.getFeaturePrefix().compareTo(arg1.getFeaturePrefix());
 			}			
 		}); //for unittests, steffans idea; I thinks that is bad and even unnecessary
 		
 		for (FeatureAppender<IExtendedFeatureAppender> ap : values) {
 			//alle appender durchlaufen und der url hinzufügen
 			urlElements.offer(ap.getAppendableString(values));
 		}
 		// for (IExtendedFeatureAppender ap : appenders){
 		// urlElements.offer(ap.getAppendableString(appenders));
 	}
 
 	protected String generateUrlString(String baseUrl) {
 		StringBuilder url = new StringBuilder();
 		url.append(baseUrl); //Standardpfad zur API
 		url.append(urlElements.poll());//charttype anhängen
 
 		while (urlElements.size() > 0) {
 			//solange noch etwas drin, an die url mit dem Trennzeichen & anhängen
 			String urlElem = urlElements.poll();
 			if (urlElem.length()>0){
 				url.append(AMPERSAND_SEPARATOR + urlElem);
 			}
 			 //TODO mva: & konfigurierbar auslagern
 		}
 		return url.toString();
 	}
 
 	private class FeatureAppender<T extends IExtendedFeatureAppender> extends
 			GenericAppender<T> {
 
 		public FeatureAppender(String stm) {
 			super(stm);
 		}
 
 		@Override
 		public String getAppendableString(List<? extends IFeatureAppender> otherAppenders) {
 			String appString = super.getAppendableString(otherAppenders);
 			
 			return appString.length()>0? getFeaturePrefix() + "="
 					+ appString : "";
 		}
 	}
 }
