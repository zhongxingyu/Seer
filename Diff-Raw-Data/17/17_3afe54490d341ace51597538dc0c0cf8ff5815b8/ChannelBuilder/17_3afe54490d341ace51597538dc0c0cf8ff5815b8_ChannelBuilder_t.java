 package org.vamdc.kida.tap;
 
 import java.util.Collection;
 import java.util.GregorianCalendar;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.Vector;
 
 import javax.xml.datatype.DatatypeConfigurationException;
 import javax.xml.datatype.DatatypeFactory;
 import javax.xml.datatype.XMLGregorianCalendar;
 
 import org.apache.cayenne.exp.Expression;
 import org.apache.cayenne.exp.ExpressionFactory;
 import org.apache.cayenne.query.SelectQuery;
 import org.vamdc.dictionary.VSSPrefix;
 import org.vamdc.kida.Biblio;
 import org.vamdc.kida.Channel;
 import org.vamdc.kida.ChannelHasSpecie;
 import org.vamdc.kida.ChannelValue;
 import org.vamdc.kida.Specie;
 import org.vamdc.kida.TypeChannel;
 import org.vamdc.tapservice.api.RequestInterface;
 import org.vamdc.tapservice.query.QueryMapper;
 import org.vamdc.tapservice.vss2.Prefix;
 import org.vamdc.tapservice.vss2.Query;
 import org.vamdc.xsams.common.DataSetType;
 import org.vamdc.xsams.common.DataSetsType;
 import org.vamdc.xsams.common.FitDataType;
 import org.vamdc.xsams.common.FitParametersType;
 import org.vamdc.xsams.common.FitValidityLimitsType;
 import org.vamdc.xsams.common.NamedDataType;
 import org.vamdc.xsams.common.ValueType;
 import org.vamdc.xsams.functions.FunctionType;
 import org.vamdc.xsams.functions.Functions;
 import org.vamdc.xsams.process.collisions.CollisionalProcessClassType;
 import org.vamdc.xsams.process.collisions.CollisionalTransitionType;
 import org.vamdc.xsams.schema.DataDescriptionType;
 import org.vamdc.xsams.sources.SourceType;
 import org.vamdc.xsams.util.IDs;
 
 public class ChannelBuilder {
 
 	private static void writeReactantProduct(Collection<ChannelHasSpecie> chsc,
 			Vector<Integer> tabSpeciesId,
 			CollisionalTransitionType mycollision, RequestInterface request) {
 		for (ChannelHasSpecie chs : chsc) {
 
 			// check if species have not been added
 			if (!tabSpeciesId.contains(new Integer(chs.getToSpecie().getId()))) {
 				Specie forgotSpecies = chs.getToSpecie();
 				if (forgotSpecies.isASpecialSpecies()) {
 					request.getXsamsroot().addElement(
 							SpeciesBuilder
 									.writeParticle(forgotSpecies, request));
 				} else if (forgotSpecies.isAnAtom()) {
 					request.getXsamsroot().addElement(
 							SpeciesBuilder.writeAtom(forgotSpecies, request));
 				} else {
 					request.getXsamsroot().addElement(
 							SpeciesBuilder.writeSpecies(forgotSpecies));
 				}
 				tabSpeciesId.addElement(new Integer(forgotSpecies.getId()));
 			}
 
 			if (chs.getType().equals("reactant")) {
 				if (chs.getToSpecie().isASpecialSpecies()) // CR, CRP,
 				// e-,
 				// Photon
 				{
 					// mycollision.getReactants().add(request.getXsamsroot().getStateRef(
 				} else if (chs.getToSpecie().isAnAtom()) {
 					mycollision
 							.getReactants()
 							.add(request.getXsamsroot().getSpeciesRef(
 									IDs.getSpecieID(chs.getToSpecie().getId())));
 				} else {
 					mycollision
 							.getReactants()
 							.add(request.getXsamsroot().getSpeciesRef(
 									IDs.getSpecieID(chs.getToSpecie().getId())));
 				}
 
 			} else if (chs.getType().equals("product")) {
 				if (chs.getToSpecie().isASpecialSpecies()) // CR, CRP,
 				// e-,
 				// Photon
 				{
 					// mycollision.getProducts().add(request.getXsamsroot().getStateRef()}
 				} else if (chs.getToSpecie().isAnAtom()) {
 					mycollision
 							.getProducts()
 							.add(request.getXsamsroot().getSpeciesRef(
 									IDs.getSpecieID(chs.getToSpecie().getId())));
 				} else {
 					mycollision
 							.getProducts()
 							.add(request.getXsamsroot().getSpeciesRef(
 									IDs.getSpecieID(chs.getToSpecie().getId())));
 				}
 
 			}
 
 		}
 
 	}
 	
 	private static SelectQuery getCayenneQuery(Query query){
 		Expression channelExp = null;
 		Expression prefExp=null;
 		//Loop over all defined prefixes
 		Vector<String> aliases = new Vector<String>();
 		for (Prefix pref:query.getPrefixes()){
 			VSSPrefix prefix = pref.getPrefix();
 			int index = pref.getIndex();
 			
 			//Add alias to vector
 			String strPrefix = prefix.name()+index;
 			aliases.add(strPrefix);
 			
 			if (prefix==VSSPrefix.REACTANT){//Handle REACTANT	
 				Expression chsex=ExpressionFactory.matchExp(strPrefix+".type", ChannelHasSpecie.REACTANT);
 				prefExp=QueryMapper.mapTree(query.getPrefixedTree(prefix, index), Restrictables.getAliasedChannelMap(strPrefix));//Build tree using aliases
 				if (prefExp!=null)
 					prefExp=prefExp.andExp(chsex);
 				
 			}else if (prefix==VSSPrefix.PRODUCT){//Handle PRODUCT
 				Expression chsex=ExpressionFactory.matchExp(strPrefix+".type", ChannelHasSpecie.PRODUCT);
 				prefExp=QueryMapper.mapTree(query.getPrefixedTree(prefix, index), Restrictables.getAliasedChannelMap(strPrefix));//Build tree using aliases
 				if (prefExp!=null)
 					prefExp=prefExp.andExp(chsex);
 			}else{
 				prefExp=null;
 			}
 			if (channelExp==null){//Channel exp is yet empty, just assign prefExp to it.
 				channelExp=prefExp;
 				prefExp=null;
 			}else if (prefExp!=null){
 				channelExp=channelExp.andExp(prefExp);//
 			}
 		}
		
		//add all keywords that don't require or don't have a prefix.
		prefExp=QueryMapper.mapTree(query.getPrefixedTree(null, 0), Restrictables.getAliasedChannelMap("unprefixed"));
		aliases.add("unprefixed");
		
		if (channelExp==null){//Channel exp is yet empty, just assign prefExp to it.
			channelExp=prefExp;
			prefExp=null;
		}else if (prefExp!=null){
			channelExp=channelExp.andExp(prefExp);
		}
		
 		System.out.println("Expression:"+channelExp);
 		SelectQuery q = new SelectQuery(Channel.class, channelExp);
 		
		if (aliases.size()>0)
			q.aliasPathSplits("channelHasSpecieArray", aliases.toArray(new String[0]));
 		
 		return q;
 		
 	}
 
 	
 
 	public static void buildChannels(RequestInterface request,
 			Vector<Integer> tabSpeciesId, Vector<String> tabFormulaName) {
 		// ObjectFactory factory = new org.vamdc.xsams.ObjectFactory(); //
 
 		SelectQuery atquery = getCayenneQuery(request.getQuery());
 		// atquery.setPrefetchTree(prefetchTree)
 		List<Channel> atms = (List<Channel>) request.getCayenneContext()
 				.performQuery(atquery);
 
 		for (Channel chan : atms) {
 			if (chan.getAddedStatus() == 0)
 				continue;
 			TypeChannel tc = chan.getToTypeChannel();
 			if (tc == null || tc.getAbbrev().equals("3-body"))
 				continue;
 
 			CollisionalTransitionType mycollision = new CollisionalTransitionType();
 
 			Collection<ChannelHasSpecie> chsc = chan.getChannelHasSpecieArray();
 			writeReactantProduct(chsc, tabSpeciesId, mycollision, request);
 
 			CollisionalProcessClassType process = new CollisionalProcessClassType();
 
 			/*
 			 * if ( writeIAEACode(chan.getToTypeChannel().getId()) != null ) {
 			 * process.setIAEACode(writeIAEACode(chan.getToTypeChannel().getId
 			 * ())); }
 			 */
 			mycollision.setProcessClass(process);
 
 			DataSetsType datasets = new DataSetsType();
 			Functions tabFunctions = new Functions();
 
 			Collection<ChannelValue> tChannelValues = chan
 					.getChannelValueArray();
 			// for all channel values for this reaction
 			writeChanneValues(tChannelValues, tabFormulaName, request,
 					mycollision, chan, datasets, tabFunctions);
 
 			mycollision.setDataSets(datasets);
 
 			witeFunction(tabFunctions, request);
 			request.getXsamsroot().addProcess(mycollision);
 		}
 
 	}
 
 	private static void witeFunction(Functions tabFunctions,
 			RequestInterface request) {
 		List<org.vamdc.xsams.schema.FunctionType> listFunction = tabFunctions
 				.getFunctions();
 		for (int cptFunction = 0; cptFunction < listFunction.size(); cptFunction++) {
 			request.getXsamsroot().addFunction(listFunction.get(cptFunction));
 		}
 
 	}
 
 	private static void writeChanneValues(
 			Collection<ChannelValue> tChannelValues,
 			Vector<String> tabFormulaName, RequestInterface request,
 			CollisionalTransitionType mycollision, Channel chan,
 			DataSetsType datasets, Functions tabFunctions) {
 		for (ChannelValue channelValue : tChannelValues) {
 			if (channelValue.getIsTrash() == 1)
 				continue;
 			if (channelValue.getIsTemp() == 1)
 				continue;
 			if (channelValue.getStatus() == 0)
 				continue;
 			if (!tabFormulaName.contains(channelValue.getToFormula().getName())) {
 				FunctionType function = ToolsBuilder.writeFormula(channelValue
 						.getToFormula().getName());
 				tabFormulaName
 						.addElement(channelValue.getToFormula().getName());
 				tabFunctions.getFunctions().add(function);
 
 			}
 			DataSetType channelValueDataSet = new DataSetType();
 			channelValueDataSet
 					.setDataDescription(DataDescriptionType.RATE_COEFFICIENT);
 			channelValueDataSet
 					.setDataDescription(DataDescriptionType.RATE_COEFFICIENT);
 			FitDataType values = new FitDataType();
 
 			// Temperature Range
 			FitValidityLimitsType tRange = new FitValidityLimitsType();
 			tRange.setLowerLimit(new ValueType((double) channelValue
 					.getToValidityRange().getTmin(), "K"));
 			tRange.setUpperLimit(new ValueType((double) channelValue
 					.getToValidityRange().getTmax(), "K"));
 			// TODO : relier T range et values
 
 			// Bibliography
 			Biblio cvBiblio = channelValue.getToBiblio();
 			if (cvBiblio != null) {
 				SourceType bibliography = BiblioBuilder
 						.writeBibliography(cvBiblio);
 				request.getXsamsroot().addSource(bibliography);
 
 				mycollision.addSource(bibliography);
 
 			}
 
 			FitDataType fitData = new FitDataType();
 			GregorianCalendar gCalendar = new GregorianCalendar();
 			gCalendar.setTime(channelValue.getCreatedAt());
 			XMLGregorianCalendar xmlCalendar = null;
 			try {
 				xmlCalendar = DatatypeFactory.newInstance()
 						.newXMLGregorianCalendar(gCalendar);
 			} catch (DatatypeConfigurationException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			fitData.setProductionDate(xmlCalendar);
 
 			FitParametersType alphaBetaGamma = new FitParametersType();
 			NamedDataType alpha = new NamedDataType();
 			alpha.setValue(new ValueType(channelValue.getValue("alpha"), chan
 					.getUnitAlpha()));
 			alpha.setName("alpha");
 			alphaBetaGamma.getFitParameters().add(alpha);
 			NamedDataType beta = new NamedDataType();
 			beta.setValue(new ValueType(channelValue.getValue("beta"), "undef"));
 			beta.setName("beta");
 			alphaBetaGamma.getFitParameters().add(beta);
 			NamedDataType gamma = new NamedDataType();
 			gamma.setValue(new ValueType(channelValue.getValue("gamma"), "keV"));
 			gamma.setName("gamma");
 			alphaBetaGamma.getFitParameters().add(gamma);
 
 			values.setFitParameters(alphaBetaGamma);
 
 			channelValueDataSet.getFitDatas().add(values);
 			datasets.getDataSets().add(channelValueDataSet);
 
 		}
 
 	}
 }
