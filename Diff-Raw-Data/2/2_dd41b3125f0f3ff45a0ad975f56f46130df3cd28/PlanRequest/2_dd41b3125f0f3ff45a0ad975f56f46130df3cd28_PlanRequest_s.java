 package com.socialcomputing.wps.server.webservices;
 
 import java.sql.Connection;
 import java.util.Enumeration;
 import java.util.Hashtable;
 import java.util.StringTokenizer;
 
 import com.socialcomputing.wps.client.applet.Env;
 import com.socialcomputing.wps.client.applet.Transfo;
 import com.socialcomputing.wps.server.generator.AttributeLink;
 import com.socialcomputing.wps.server.generator.ProtoAttribute;
 import com.socialcomputing.wps.server.generator.Recommendable;
 import com.socialcomputing.wps.server.generator.RecommendationGroup;
 import com.socialcomputing.wps.server.persistence.Swatch;
 import com.socialcomputing.wps.server.persistence.hibernate.SwatchManagerImpl;
 import com.socialcomputing.wps.server.plandictionary.AffinityReaderProfile;
 import com.socialcomputing.wps.server.plandictionary.AnalysisProfile;
 import com.socialcomputing.wps.server.plandictionary.Model;
 import com.socialcomputing.wps.server.plandictionary.WPSDictionary;
 import com.socialcomputing.wps.server.plandictionary.WPSSelection;
 import com.socialcomputing.wps.server.plandictionary.connectors.WPSConnectorException;
 import com.socialcomputing.wps.server.plandictionary.connectors.iSelectionConnector;
 import com.socialcomputing.wps.server.swatchs.InterValue;
 import com.socialcomputing.wps.server.swatchs.ValueContainer;
 import com.socialcomputing.wps.server.swatchs.XSwatch;
 
 
 public class PlanRequest
 {
 	/**
 	 * This Hashtable contains specific internet user parameters or constraints :
 	 * "entityId" : reference entity
 	 * "attributeId" : reference attribute for discovery plans
 	 * "affinityReaderProfile" : force the affinity reader profile
 	 * "analysisProfile" : force the analysis profile
 	 * "displayProfile" : force the display profile
 	 * "appletSize" : dimension of the Applet ( ex: "appletSize=300,400")
 	 */
 	private Hashtable<String, Object>   m_RequestParameters = null;
 	private Hashtable<String, XSwatch>	m_LoadedSwatch = null;
 	private AnalysisProfile        		m_AnalysisProfile = null;
 	private AffinityReaderProfile  		m_AffinityReaderProfile = null;
 	private Model                  		m_Model = null; // display profile
 
 	public WPSDictionary m_Dictionary = null;
 
 	// Identifiant de l'entit� du plan personnel
 	public String m_entityId = null;
 	// Identifiant de l'attribut du plan de découverte
 	public String m_discoveryAttributeId = null;
 	// Identitede segmentation
 	public RequestingClassifyId m_classifyId = null;
 
 	public PlanRequest( Connection WPSConnection, WPSDictionary dico, Hashtable<String, Object> parameters ) throws WPSConnectorException
 	{
 		m_LoadedSwatch = new Hashtable<String, XSwatch>();
 		m_Dictionary = dico;
 		m_RequestParameters = parameters;
 		m_entityId = this.getParameter( "entityId");
 		m_classifyId = new RequestingClassifyId( this.getParameter( "classifyId") != null ? this.getParameter( "classifyId") : m_entityId);
 
 		{   // Initialisation des AffinityReader par defauts (built-in)
 			for( AffinityReaderProfile profile : m_Dictionary.m_AffinityReaderProfiles.values())
 			{
 				if( profile.m_defaultConnector != null)
 					profile.m_defaultConnector.instanciate( WPSConnection, this);
 			}
 		}
 
 		AnalysisProfile analysisProfile = this.getAnalysisProfile();
 		switch( analysisProfile.m_planType)
 		{
 			case AnalysisProfile.PERSONAL_PLAN:
 				if( m_entityId == null)
 					throw new WPSConnectorException( "WPS Server : entityId parameter is not set for personal plan");
 				break;
 			case AnalysisProfile.DISCOVERY_PLAN:
 				m_discoveryAttributeId = getParameter( "attributeId");
 				if( m_discoveryAttributeId == null)
 					throw new WPSConnectorException( "WPS Server : attributeId parameter is not set for discovery plan");
 				if( analysisProfile.m_AttributesRef.indexOf( '=') != -1 || analysisProfile.m_AttributesRef.indexOf( '&') != -1)
 				{
 					StringTokenizer st = new StringTokenizer( analysisProfile.m_AttributesRef, "&");
 					for( int i = 0; st.hasMoreTokens(); ++i)
 					{
 						String profile = st.nextToken();
 						int pos = profile.indexOf( "=");
 						if( pos != -1)
 						{
 							String prefix = profile.substring( pos+1);
 							if( m_discoveryAttributeId.startsWith( prefix))
 								this.m_RequestParameters.put( "~$connectorAttributeId", m_discoveryAttributeId.substring( prefix.length()));
 						}
 					}
 				}
 				else
 					this.m_RequestParameters.put( "~$connectorAttributeId", m_discoveryAttributeId);
 				break;
 		}
 	}
 
 	public String getParameter( String name )
 	{
 		return (String)m_RequestParameters.get( name );
 	}
 
 	public String getParameter( String name, String defaultPrm )
 	{
 		String  param   = (String)m_RequestParameters.get( name );
 
 		return param == null ? defaultPrm : param;
 	}
 
 	public AnalysisProfile getAnalysisProfile() throws WPSConnectorException
 	{
 		if( m_AnalysisProfile != null) // Speeder
 			return m_AnalysisProfile;
 		String profile = this.getParameter( "analysisProfile");
 		if ( profile == null)
 		{  // Analysis profile is dictionary defined
 		   m_AnalysisProfile = m_Dictionary.getAnalysisProfile( m_classifyId);
 		}
 		else
 		{   // Analysis profile is applet defined
 			m_AnalysisProfile = m_Dictionary.getAnalysisProfile( profile);
 			if( m_AnalysisProfile == null) // Error : set to default
 				m_AnalysisProfile = m_Dictionary.getAnalysisProfile( m_classifyId);
 		}
 		return m_AnalysisProfile;
 	}
 
 	public AffinityReaderProfile getAffinityReaderProfile() throws WPSConnectorException
 	{
 		if( m_AffinityReaderProfile != null)
 			return m_AffinityReaderProfile;
 		String grpName = this.getParameter( "affinityReaderProfile");
 		if ( grpName == null)
 		{  // AffinityGroupReader is dictionary defined
 		   m_AffinityReaderProfile = m_Dictionary.getAffinityReaderProfile( this.getAnalysisProfile().m_Name, m_classifyId);
 		}
 		else
 		{   // AffinityGroupReader is applet defined
 			m_AffinityReaderProfile = m_Dictionary.getAffinityReaderProfile( grpName);
 			if( m_AffinityReaderProfile == null) // Error : set to default
 				m_AffinityReaderProfile = m_Dictionary.getAffinityReaderProfile( this.getAnalysisProfile().m_Name, m_classifyId);
 		}
 		return m_AffinityReaderProfile;
 	}
 
 	public  Model getModel() throws WPSConnectorException
 	{
 		if( m_Model != null) // Speeder
 			return m_Model;
 		String modelName = this.getParameter( "displayProfile");
 		if ( modelName == null)
 		{  // Model is dictionary defined
 		   m_Model = m_Dictionary.getModel( this.getAnalysisProfile().m_Name, getParameter( "language", "en"), m_classifyId);
 		}
 		else
 		{   // Model is applet defined
 			m_Model = ( Model) m_Dictionary.m_Models.get( modelName);
 			if( m_Model == null) // Error : set to default
 				m_Model = m_Dictionary.getModel( this.getAnalysisProfile().m_Name, getParameter( "language", "en"), m_classifyId);
 		}
 		return m_Model;
 	}
 
 	/**
 	 * Returns the property list of this Group plus a "SELECTION" property (32bits Integer).
 	 * The Model find the Swatch corresponding to this id and calls getBoundNames.
 	 * Those names can be cached by the ServerSwatch
 	 */
 	public Hashtable<String, Object> getEntityProps( String swatchName, String id ) throws WPSConnectorException
 	{
 		// DB properties
 		Hashtable<String, Object> dbProperties = m_Dictionary.getEntityConnector().getProperties( id);
 
 		Hashtable<String, Object> swatchProperties = new Hashtable<String, Object>();
 		XSwatch swatch = this.getSwatch( swatchName);
 
 		AnalysisProfile profile = this.getAnalysisProfile();
 		Model model = this.getModel();
 
 		// Generator used swatch properties
 		ValueContainer[]    vals    = swatch.getProps();
 		ValueContainer      val;
 		String[]            propNames;
 		String              name, dbPropertyName;
 		Object              prop;
 		int                 i, j, pCnt, vCnt = vals.length;
 
 		for ( j  = 0; j < vCnt; j ++ )
 		{
 			val         = vals[j];
 			propNames   = val.getProps();
 			pCnt        = propNames.length;
 
 			for ( i = 0; i < pCnt; i ++ )
 			{
 				name            = propNames[i];
 				dbPropertyName  = profile.getEntitySwatchProperty( name );
 				if( dbPropertyName != null)
 				{
 					prop            = ExtractProperty( dbPropertyName, dbProperties);
 					if ( prop != null )
 					{
 						Object v = val.getValue( name, prop );
 						if( v == null)
 							throw new WPSConnectorException( "Entity '" + id + "' property '" + name + "' can't be evaluated ");
 						swatchProperties.put( name, v);
 					}
 				}
 			}
 		}
 
 		// SELECTION property
 		int propSelection = 0, mask = 1,
 			sCnt = getSelectionCount();
 
 		for (i = 0; i < sCnt; mask <<= 1, ++i)
 		{
 			if( model.m_EntitiesSelections[i] != null)
 			{
 				iSelectionConnector selCon = m_Dictionary.getEntityConnector().getSelection( model.m_EntitiesSelections[i].m_SelectionRef);
 				if( (selCon != null) && selCon.isRuleVerified( id, false, this.m_entityId))
 					propSelection |= mask;
 			}
 		}
 		swatchProperties.put( "SELECTION", new Integer( propSelection));
 		return swatchProperties;
 	}
 
 	public void updateAttLinkInterProps( AttributeLink link, XSwatch restSwh, XSwatch curSwh, Hashtable props ) throws WPSConnectorException
 	{
 		updateInterProps( null, restSwh, curSwh, props );
 	}
 
 	public void updateInterProps( ProtoAttribute att, XSwatch restSwh, XSwatch curSwh, Hashtable props ) throws WPSConnectorException
 	{
 		// Generator used swatch properties
 		InterValue[]    restVals        = restSwh.getAdaptiveInterValues(),
 						curVals         = curSwh.getAdaptiveInterValues();
 		InterValue      val;
 		String          name;
 		Object          prop;
 		int             i,
 						rCnt            = restVals.length,
 						vCnt            = rCnt + curVals.length;
 
 		for ( i = 0; i < vCnt; i ++ )
 		{
 			val     = i < rCnt ? restVals[i] : curVals[i-rCnt];
 			name    = val.getProps()[0];
 			prop    = props.get( name ); // Previously stored Prop
 
 			if ( prop != null )
 			{
 				Object v = val.getValue( name, prop );
 				if( v == null)
 					throw new WPSConnectorException( "Link property '" + name + "' can't be evaluated ");
 			}
 		}
 	}
 
 	/**
 	 * Returns the property list of this Node plus a "SELECTION" property (32bits Integer).
 	 * The Model find the Swatch corresponding to this id and calls getBoundNames.
 	 * Those names can be cached by the XSwatch.
 	 */
 	public  void putAttributeProps( Recommendable recommendable, XSwatch restSwh, XSwatch curSwh, Hashtable<String, Object> props ) throws WPSConnectorException
 	{
 		// DB properties
 		boolean         isBase          = recommendable.isRef();
 		String          strId           = recommendable.getStrId();
 		Hashtable       dbProperties    = getAnalysisProfile().getConnector( m_Dictionary).getProperties( strId, isBase, m_entityId);
 		Model           model           = getModel();
 		AnalysisProfile profile         = this.getAnalysisProfile();
 
 		// Generator used swatch properties
 		ValueContainer[]    restVals    = restSwh.getProps(),
 							curVals     = curSwh.getProps();
 		ValueContainer      val;
 		String[]            propNames;
 		String              name, dbPropertyName;
 		Object              prop    = null;
 		int                 i, j, pCnt,
 							rCnt = restVals.length,
 							vCnt = rCnt + curVals.length;
 		//boolean             isInner;
 
 		Hashtable []tabRecoProps = new Hashtable[ RecommendationGroup.RECOM_TYPE_CNT];
 		if ( recommendable.hasRecos())
 		{
 			for( i = 0; i < RecommendationGroup.RECOM_TYPE_CNT; ++i)
 			{
 				if( recommendable.hasReco( i ) && profile.m_RecomProfiles[ i] != null)
 				{
 					tabRecoProps[i] = transformRecommendation( recommendable, profile, i );
 				}
 			}
 		}
 
 		for ( j = 0; j < vCnt; j ++ )
 		{
 			val         = j < rCnt ? restVals[j] : curVals[j-rCnt];
 			propNames   = val.getProps();
 			pCnt        = propNames.length;
 
 			for ( i = 0; i < pCnt; i ++ )
 			{
 				name    = propNames[i];
 
 				if ( name.charAt( 0 ) == '~' )  name = propNames[i+1];// Inner Prop
 				if ( name.charAt( 0 ) == '$' )  continue;
 
 				dbPropertyName  = profile.getAttributeSwatchProperty( name );
 				prop = null;
 				if( dbPropertyName == null)
 				{   // May be a motor property, look at Recommendations
 					for( int index = 0; index < RecommendationGroup.RECOM_TYPE_CNT; ++index)
 					{
 						if( tabRecoProps[index] != null)
 						{
 							dbPropertyName = profile.m_RecomProfiles[ index].getSwatchProperty( name);
 							if( dbPropertyName != null)
 								prop = tabRecoProps[index].get( dbPropertyName);
 							if( prop != null)
 								break;
 						}
 					}
 				}
 				else
 				{   // DB Property
 					prop            = ExtractProperty( dbPropertyName, dbProperties);
 				}
 
 				if ( prop != null )
 				{
 					name    = propNames[i];
 
 					if ( val.isAdaptive())
 					{
 						((InterValue)val ).updateRange( prop );
 						props.put( name, prop );
 					}
 					else if ( !props.containsKey( name ))
 					{
 						Object v = val.getValue( name, prop );
 						if( v == null)
 							throw new WPSConnectorException( "Attribute '" + strId + "' property '" + name + "' can't be evaluated ");
 						props.put( name, v);
 					}
 				}
 			}
 		}
 
 		// SELECTION property
 		int propSelection = 0, mask = 1,
 			sCnt = getSelectionCount();
 
 		for (i = 0; i < sCnt; mask <<= 1, ++i)
 		{
 			WPSSelection sel = model.m_AttributesSelections[i];
 			if( (sel != null) && !sel.m_FreeSelection)
 			{
 				iSelectionConnector selCon = getAnalysisProfile().getConnector( m_Dictionary).getSelection( model.m_AttributesSelections[i].m_SelectionRef);
 				if( (selCon != null) && selCon.isRuleVerified( strId, isBase, this.m_entityId))
 					propSelection |= mask;
 			}
 		}
 
 		props.put( "SELECTION", new Integer( propSelection));
 	}
 
 	/**
 	 * Returns the property list of this Link plus a "SELECTION" property (32bits Integer).
 	 * The Model find the Swatch corresponding to this id and calls getBoundNames.
 	 * Those names can be cached by the XSwatch.
 	 */
 	public  void putAttLinkProps( Recommendable recommendable, XSwatch restSwh, XSwatch curSwh, Hashtable props ) throws WPSConnectorException
 	{
 		boolean				isReal			= recommendable != null;
 		AnalysisProfile     profile         = isReal ? getAnalysisProfile() : null;
 		ValueContainer[]    restVals        = restSwh.getProps(),
 							curVals         = curSwh == null ? null : curSwh.getProps();
 		ValueContainer      val;
 		Hashtable[]         tabRecoProps    = null;
 		Object              prop            = null;
 		String[]            propNames;
 		String              name, dbPropertyName;
 		int                 i, j, pCnt,
 							rCnt            = restVals.length,
 							vCnt            = curVals == null ? rCnt : rCnt + curVals.length;
 		//boolean             isInner;
 
 		if ( vCnt > 0 && isReal )
 		{
 			tabRecoProps = new Hashtable[ RecommendationGroup.RECOM_TYPE_CNT];
 
 			if ( recommendable.hasRecos())
 			{
 				for( i = 0; i < RecommendationGroup.RECOM_TYPE_CNT; ++i)
 				{
 					if( recommendable.hasReco( i ) && profile.m_RecomProfiles[ i] != null)
 					{
 						tabRecoProps[i] = transformRecommendation( recommendable, profile, i );
 					}
 				}
 			}
 		}
 
 		for ( j = 0; j < vCnt; j ++ )
 		{
 			val         = j < rCnt ? restVals[j] : curVals[j-rCnt];
 			propNames   = val.getProps();
 			pCnt        = propNames.length;
 
 			for ( i = 0; i < pCnt; i ++ )
 			{
 				name    = propNames[i];
 
 				if ( name.charAt( 0 ) == '~' )  name = propNames[i+1];// Inner Prop
 				if ( name.charAt( 0 ) == '$' )  continue;
 
 				prop = null;
 
 				if ( isReal )
 				{
 					for( int index = 0; index < RecommendationGroup.RECOM_TYPE_CNT; ++index)
 					{
 						if( tabRecoProps[index] != null)
 						{
 							dbPropertyName = profile.m_RecomProfiles[ index].getSwatchProperty( name);
 							if( dbPropertyName != null)
 								prop = tabRecoProps[index].get( dbPropertyName);
 							if( prop != null)
 								break;
 						}
 					}
 				}
 
 				// On the fly PlanGenerator properties
 				if ( prop == null )
 				{
 					prop	= props.get( name );
 				}
 
 				if ( prop != null )
 				{
 					name    = propNames[i];
 
 					if ( val.isAdaptive())
 					{
 						((InterValue)val ).updateRange( prop );
 						props.put( name, prop );
 					}
 					else if ( !props.containsKey( name ))
 					{
 						props.put( name, val.getValue( name, prop ));
 					}
 				}
 			}
 		}
 
 		// SELECTION property
 //		int propSelection = 0, mask = 1,
 //			sCnt = getSelectionCount();
 //
 //		props.put( "SELECTION", new Integer( propSelection));
 	}
 
 	private Hashtable<String, Object> transformRecommendation( Recommendable recommendable, AnalysisProfile profile, int recIndex ) throws WPSConnectorException
 	{
 		String[]    strIds  = (String[])recommendable.getRecommendations( recIndex ).toArray( new String[0] );
 		Hashtable<String, Object>   props   = new Hashtable<String, Object>();
 		int         i, size = strIds.length;
 
 		for ( i = 0; i < size; i++ )
 		{
 			Hashtable dbProps = null;
 			switch( recIndex)
 			{
 				case RecommendationGroup.ATTRIBUTES_RECOM:
 					 dbProps = profile.getConnector( m_Dictionary).getProperties( strIds[i], recommendable.isRef(), m_entityId);
 					 break;
 				case RecommendationGroup.ENTITIES_RECOM:
 					 dbProps = m_Dictionary.getEntityConnector().getProperties( strIds[i]);
 					 break;
 				case RecommendationGroup.SATTRIBUTES_RECOM:
 					 dbProps = profile.getConnector( m_Dictionary).getSubAttribute().getProperties( strIds[i], null, m_entityId);
 					 break;
 			}
 			if( dbProps != null)
 			{
 				//Set set = dbProps.keySet();
 				Enumeration enumvar = dbProps.keys();
 				while( enumvar.hasMoreElements())
 				{
 					String p = ( String) enumvar.nextElement();
 					Object[] tab = (Object[] )props.get( p);
 					if( tab == null)
 					{
 						tab = new Object[ size];
 						props.put( p, tab);
 					}
 					tab[i] = dbProps.get( p);
 				}
 			}
 		}
 		return props;
 	}
 
 	public int getSelectionCount( )
 	{
 		return Model.MAX_SELECTION;
 	}
 
 	/**
 	 * Returns the Swatch of the specified type used by all Nodes/Links/Clusters/Adds
 	 * or the Reference Object. Groups are segmented so we need to use another methode.
 	 * @see #getGroupSwatchName
 	 * @see #getGroupSwatch
 	 */
 	public  XSwatch getSwatch( int type, int style ) throws WPSConnectorException
 	{
 		Model   model       = getModel();
 		String  swatchName  = null;
 
 		switch( type )
 		{
 			case XSwatch.GROUP_TYP:
 				 return null;
 
 			case XSwatch.NODE_TYP:
 				 swatchName = model.getAttributeSwatch( m_classifyId, style );
 				 break;
 
 			case XSwatch.LINK_TYP:
 				 swatchName = model.getLinkSwatch( m_classifyId, style );
 				 if ( style > 1 && swatchName.equals(  WPSDictionary.DEFAULT_NAME ))    return null;
 				 break;
 
 			case XSwatch.CLUS_TYP:
 				 swatchName = model.getClusterSwatch( m_classifyId, style );
 				 break;
 
 			case XSwatch.YAH_TYP:
 				 swatchName = model.getReferenceSwatch( m_classifyId, style );
 				 break;
 
 			case XSwatch.ADD_TYP:
 				 swatchName = model.getAdvertisingSwatch( m_classifyId, style );
 				 break;
 
 			default:
 				return null;
 		}
 
 		return  getSwatch( swatchName );
 	}
 
 	/**
 	 * Returns the named Swatch. Useful to retrieve the Swatchs after we've got their names.
 	 */
 	public XSwatch getSwatch( String swatchName) throws WPSConnectorException
 	{
 		XSwatch swatch = null;
 
 		if ( swatchName != null )
 		{
 			swatch = m_LoadedSwatch.get( swatchName );
 
 			if( swatch == null )
 			{
 				try
 				{
 					//SwatchLoader loader = m_SwatchLoaderHome.findByPrimaryKey( swatchName);
 					//swatch = loader.getSwatch();
 					SwatchManagerImpl manager =  new SwatchManagerImpl();
					Swatch swatchLoader = manager.findByName(swatchName);					
 					swatch = swatchLoader.getSwatch();
 					
 					m_LoadedSwatch.put( swatchName, swatch );
 				}
 				catch( Exception e)
 				{
 					throw new WPSConnectorException( "WPS server unable to find swatch", e);
 					//e.printStackTrace();
 				}
 			}
 		}
 
 		return swatch;
 	}
 
 	public Env initEnv( ) throws WPSConnectorException
 	{
 		Model model = this.getModel();
 		Env env = new Env();
 
 		// Donn�es issues du mod�le
 		model.initClientEnv( m_Dictionary, env);
 
 		// Propri�t�s globales venant de la demande de plan
 		Enumeration enumvar = m_RequestParameters.keys();
 		while( enumvar.hasMoreElements())
 		{
 			String name = ( String)enumvar.nextElement();
 			Object value = m_RequestParameters.get( name);
 			if( value instanceof String)
 			{
 				if( name.startsWith( "$"))
 					env.m_props.put( name, value);
 				else
 					env.m_props.put( "$" + name, value);
 			}
 		}
 
 		// Autres donn�es
 		try
 		{
 			env.m_transfo = new Transfo
 			(
 				Float.parseFloat( getParameter( "width", "640" )),
 				Float.parseFloat( getParameter( "height", "480" )),
 				1.f,
 				Transfo.CART_BIT | Transfo.ABS_BIT
 			);
 		}
 		catch ( NumberFormatException e )
 		{
 			env.m_transfo = new Transfo
 			(
 				640,
 				480,
 				1.f,
 				Transfo.CART_BIT | Transfo.ABS_BIT
 			);
 		}
 
 		return env;
 	}
 
 	static public Object ExtractProperty( String definition, Hashtable<String, Object> properties) throws WPSConnectorException
 	{
 		if( definition.indexOf( '&') == -1)
 			return properties.get( definition);
 
 		StringBuffer result = new StringBuffer();
 		StringTokenizer st = new StringTokenizer( definition, "&");
 		for( int i = 0; st.hasMoreTokens(); ++i)
 		{
 			String item = st.nextToken();
 			if( item.startsWith( "\"") && item.endsWith( "\""))
 				result.append( item.substring( 1, item.length()-1));
 			else
 			{
 				String value = (String)properties.get( item);
 				//if( value == null)
 					//throw new WPSConnectorException( "Property '" + item + "' in '" + definition + "' can't be evaluated ");
 				if( value != null)
 					result.append( value);
 			}
 		}
 		return result.toString();
 	}
 }
