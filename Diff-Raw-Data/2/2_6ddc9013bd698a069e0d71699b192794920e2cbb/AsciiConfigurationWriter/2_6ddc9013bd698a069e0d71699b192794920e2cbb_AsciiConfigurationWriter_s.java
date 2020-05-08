 package confdb.converter.ascii;
 
 import java.util.Iterator;
 
 import confdb.converter.ConverterEngine;
 import confdb.converter.ConverterException;
 import confdb.converter.IConfigurationWriter;
 import confdb.converter.IEDSourceWriter;
 import confdb.converter.IESSourceWriter;
 import confdb.converter.IESModuleWriter;
 import confdb.converter.IModuleWriter;
 import confdb.converter.IParameterWriter;
 import confdb.converter.IPathWriter;
 import confdb.converter.ISequenceWriter;
 import confdb.converter.IServiceWriter;
 import confdb.data.Block;
 import confdb.data.IConfiguration;
 import confdb.data.EDSourceInstance;
 import confdb.data.ESSourceInstance;
 import confdb.data.ESModuleInstance;
 import confdb.data.ModuleInstance;
 import confdb.data.Parameter;
 import confdb.data.Path;
 import confdb.data.Sequence;
 import confdb.data.ServiceInstance;
 
 public class AsciiConfigurationWriter implements IConfigurationWriter 
 {
 	protected ConverterEngine converterEngine = null;
 
 	public String toString( IConfiguration conf, WriteProcess writeProcess  ) throws ConverterException
 	{
 		String indent = "  ";
 		StringBuffer str = new StringBuffer( 100000 );
 		String fullName = conf.parentDir().name() + "/" + conf.name() + "/V" + conf.version() ;		
 		str.append( "// " + fullName
   		   + "  (" + conf.releaseTag() + ")" + converterEngine.getNewline() + converterEngine.getNewline() );
 
 		if ( writeProcess == WriteProcess.YES )
 			str.append( "process " + conf.processName() + " = {" + converterEngine.getNewline() );
 		else
 			indent = "";
 
 		ISequenceWriter sequenceWriter = converterEngine.getSequenceWriter();
 		for ( int i = 0; i < conf.sequenceCount(); i++ )
 		{
 			Sequence sequence = conf.sequence(i);
 			str.append( sequenceWriter.toString(sequence, converterEngine, indent ) );
 		}
 
 		IPathWriter pathWriter = converterEngine.getPathWriter();
 		for ( int i = 0; i < conf.pathCount(); i++ )
 		{
 			Path path = conf.path(i);
 			str.append( pathWriter.toString( path, converterEngine, indent ) );
 		}
 
 		IParameterWriter parameterWriter = converterEngine.getParameterWriter();
 		for ( int i = 0; i < conf.psetCount(); i++ )
 		{
 			Parameter pset = conf.pset(i);
 			str.append( parameterWriter.toString( pset, converterEngine, indent ) );
 		}
 
 
 		IEDSourceWriter edsourceWriter = converterEngine.getEDSourceWriter();
 		for ( int i = 0; i < conf.edsourceCount(); i++ )
 		{
 			EDSourceInstance edsource = conf.edsource(i);
 			str.append( edsourceWriter.toString(edsource, converterEngine, indent ) );
 		}
 
 		IESSourceWriter essourceWriter = converterEngine.getESSourceWriter();
 		for ( int i = 0; i < conf.essourceCount(); i++ )
 		{
 			ESSourceInstance essource = conf.essource(i);
 			str.append( essourceWriter.toString(essource, converterEngine, indent ) );
 		}
 
 
 		IESModuleWriter esmoduleWriter = converterEngine.getESModuleWriter();
 		for ( int i = 0; i < conf.esmoduleCount(); i++ )
 		{
 			ESModuleInstance esmodule = conf.esmodule(i);
 			str.append( esmoduleWriter.toString( esmodule, converterEngine, indent ) );
 		}
 
 
 		IServiceWriter serviceWriter = converterEngine.getServiceWriter();
 		for ( int i = 0; i < conf.serviceCount(); i++ )
 		{
 			ServiceInstance service = conf.service(i);
 			str.append( serviceWriter.toString( service, converterEngine, indent ) );
 		}
 
 		IModuleWriter moduleWriter = converterEngine.getModuleWriter();
 		for ( int i = 0; i < conf.moduleCount(); i++ )
 		{
 			ModuleInstance module = conf.module(i);
 			str.append( moduleWriter.toString( module ) );
 		}
 
 		Iterator<Block> blockIterator = conf.blockIterator();
 		while ( blockIterator.hasNext() )
 		{
 			Block block = blockIterator.next();
			str.append( indent + "block " + block.name() + " {\n" );
 			Iterator<Parameter> parameterIterator = block.parameterIterator();
 			while ( parameterIterator.hasNext() )
 			{
 				str.append( parameterWriter.toString( parameterIterator.next(), converterEngine, indent + "  " ) );
 			}
 			str.append( indent + "}\n" );
 		}
 
 		if ( writeProcess == WriteProcess.YES )
 			str.append( converterEngine.getConfigurationTrailer() );
 		return str.toString();
 	}
 
 	public void setConverterEngine( ConverterEngine converterEngine ) 
 	{
 		this.converterEngine = converterEngine;
 	}
 	
 }
