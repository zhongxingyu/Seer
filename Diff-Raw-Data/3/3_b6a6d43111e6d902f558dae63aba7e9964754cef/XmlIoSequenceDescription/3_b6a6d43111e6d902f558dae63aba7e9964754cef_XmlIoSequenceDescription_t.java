 package mpicbg.spim.data.sequence;
 
 import static mpicbg.spim.data.sequence.XmlKeys.SEQUENCEDESCRIPTION_TAG;
 
 import java.io.File;
 import java.util.ArrayList;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 
 public class XmlIoSequenceDescription< T extends TimePoint, V extends ViewSetup >
 {
 	protected final XmlIoTimePointsAbstract< T > xmlTimePoints;
 
 	protected final XmlIoViewSetupsAbstract< V > xmlViewSetups;
 
 	protected final XmlIoMissingViews xmlMissingViews;
 
 	protected final XmlIoImgLoader xmlImgLoader;
 
 	/**
 	 * TODO
 	 */
 	public String getTagName()
 	{
 		return SEQUENCEDESCRIPTION_TAG;
 	}
 
 	public XmlIoSequenceDescription(
 			final XmlIoTimePointsAbstract< T > xmlTimePoints,
 			final XmlIoViewSetupsAbstract< V > xmlViewSetups,
 			final XmlIoMissingViews xmlMissingViews,
 			final XmlIoImgLoader xmlImgLoader )
 	{
 		this.xmlTimePoints = xmlTimePoints;
 		this.xmlViewSetups = xmlViewSetups;
 		this.xmlMissingViews = xmlMissingViews;
 		this.xmlImgLoader = xmlImgLoader;
 	}
 
 	public static XmlIoSequenceDescription< TimePoint, ViewSetup > createDefault()
 	{
 		return new XmlIoSequenceDescription< TimePoint, ViewSetup >( new XmlIoTimePoints(), new XmlIoViewSetups(), new XmlIoMissingViews(), new XmlIoImgLoader() );
 	}
 
 	/**
 	 * Read a {@link SequenceDescription} from the specified DOM element.
 	 *
 	 * @param element
 	 *            a {@value XmlKeys#SEQUENCEDESCRIPTION_TAG} DOM element.
 	 * @return sequence represented by the specified DOM element.
 	 * @throws ClassNotFoundException
 	 * @throws IllegalAccessException
 	 * @throws InstantiationException
 	 */
 	public SequenceDescription< T, V > fromXml( final Element sequenceDescription, final File basePath ) throws InstantiationException, IllegalAccessException, ClassNotFoundException
 	{
 		NodeList nodes = sequenceDescription.getElementsByTagName( xmlTimePoints.getTagName() );
 		if ( nodes.getLength() == 0 )
 			throw new IllegalArgumentException( "no <" + xmlTimePoints.getTagName() + "> element found." );
 		final TimePoints< T > timepoints = xmlTimePoints.fromXml( ( Element ) nodes.item( 0 ) );
 
 		nodes = sequenceDescription.getElementsByTagName( xmlViewSetups.getTagName() );
 		if ( nodes.getLength() == 0 )
 			throw new IllegalArgumentException( "no <" + xmlViewSetups.getTagName() + "> element found." );
 		final ArrayList< V > setups = xmlViewSetups.fromXml( ( Element ) nodes.item( 0 ) );
 
 		MissingViews missingViews = null;
 		nodes = sequenceDescription.getElementsByTagName( xmlMissingViews.getTagName() );
 		if ( nodes.getLength() > 0 )
 			missingViews = xmlMissingViews.fromXml( ( Element ) nodes.item( 0 ) );
 
 		ImgLoader imgLoader = null;
 		try
 		{
 			nodes = sequenceDescription.getElementsByTagName( xmlImgLoader.getTagName() );
 			if ( nodes.getLength() > 0 )
 				imgLoader = xmlImgLoader.fromXml( ( Element ) nodes.item( 0 ), basePath );
 		}
 		catch ( final ClassNotFoundException e )
 		{
 			e.printStackTrace( System.err );
 		}
 
 		return new SequenceDescription< T, V >( timepoints, setups, missingViews, imgLoader );
 	}
 
 	public Element toXml( final Document doc, final SequenceDescription< T, V > sequenceDescription, final File basePath )
 	{
 		final Element elem = doc.createElement( SEQUENCEDESCRIPTION_TAG );
 		if ( sequenceDescription.getImgLoader() != null )
 			elem.appendChild( xmlImgLoader.toXml( doc, basePath, sequenceDescription.getImgLoader() ) );
 		elem.appendChild( xmlViewSetups.toXml( doc, sequenceDescription.getViewSetups() ) );
 		elem.appendChild( xmlTimePoints.toXml( doc, sequenceDescription.getTimePoints() ) );
		if ( sequenceDescription.getMissingViews() != null && sequenceDescription.getMissingViews().missingViews.size() > 0 )
			elem.appendChild( xmlMissingViews.toXml( doc, sequenceDescription.getMissingViews() ) );
 		return elem;
 	}
 }
