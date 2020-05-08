 package cz.cuni.mff.xrg.intlib.extractor.simplexslt;
 
 import cz.cuni.xrg.intlib.commons.configuration.ConfigException;
 import cz.cuni.xrg.intlib.commons.configuration.Configurable;
 
 import cz.cuni.xrg.intlib.commons.data.DataUnitCreateException;
 import cz.cuni.xrg.intlib.commons.data.DataUnitException;
 import cz.cuni.xrg.intlib.commons.data.DataUnitType;
 import cz.cuni.xrg.intlib.commons.dpu.DPU;
 import cz.cuni.xrg.intlib.commons.dpu.DPUContext;
 import cz.cuni.xrg.intlib.commons.dpu.DPUException;
 import cz.cuni.xrg.intlib.commons.dpu.annotation.AsExtractor;
 import cz.cuni.xrg.intlib.commons.dpu.annotation.OutputDataUnit;
 import cz.cuni.xrg.intlib.commons.module.dpu.ConfigurableBase;
 import cz.cuni.xrg.intlib.commons.web.AbstractConfigDialog;
 import cz.cuni.xrg.intlib.commons.web.ConfigDialogProvider;
 import cz.cuni.xrg.intlib.rdf.interfaces.RDFDataUnit;
 import java.io.File;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.xml.transform.stream.StreamSource;
 import net.sf.saxon.s9api.Processor;
 import net.sf.saxon.s9api.SaxonApiException;
 import net.sf.saxon.s9api.Serializer;
 import net.sf.saxon.s9api.XdmNode;
 import net.sf.saxon.s9api.XsltCompiler;
 import net.sf.saxon.s9api.XsltExecutable;
 import net.sf.saxon.s9api.XsltTransformer;
 
 /**
  * Simple XSLT Extractor
  *
  * @author tomasknap
  */
 @AsExtractor
 public class SimpleXSLT extends ConfigurableBase<SimpleXSLTConfig> implements DPU, ConfigDialogProvider<SimpleXSLTConfig> {
     
 
     public SimpleXSLT(){
             super(new SimpleXSLTConfig());
         }
     
     @OutputDataUnit
 	public RDFDataUnit rdfDataUnit;
 
     /**
      * DPU's configuration.
      */
 //    private SimpleXSLTConfig config;
 
     @Override
     public AbstractConfigDialog<SimpleXSLTConfig> getConfigurationDialog() {
         return new SimpleXSLTDialog();
     }
 
 //    @Override
 //    public void configure(SimpleXSLTConfig c) throws ConfigException {
 //        config = c;
 //    }
 //
 //    @Override
 //    public SimpleXSLTConfig getConfiguration() {
 //        return config;
 //    }
 
     // TODO 2: Provide implementation of unimplemented methods 
     @Override
     public void execute(DPUContext context) throws DPUException, DataUnitException  {
 
         //inputs
         File stylesheet = new File(config.getXslTemplate());
 
         File inputFile = new File(config.getXmlFile());
 
         File outputFile = new File(config.getXmlFile() + ".ttl");
         
         
         
 //        //outputs
 //        RDFDataUnit outputRepository;
 //        try {
 //            outputRepository = (RDFDataUnit) context.addOutputDataUnit(DataUnitType.RDF, "output");
 //        } catch (DataUnitCreateException e) {
 //            throw new DPUException("Can't create DataUnit", e);
 //        }
 
 
 
         //xslt
         Processor proc = new Processor(false);
         XsltCompiler compiler = proc.newXsltCompiler();
         XsltExecutable exp;
         try {
             exp = compiler.compile(new StreamSource(stylesheet));
 
 
             XdmNode source = proc.newDocumentBuilder().build(new StreamSource(inputFile));
 
 
 
             Serializer out = new Serializer();
             out.setOutputProperty(Serializer.Property.METHOD, "text");
             out.setOutputProperty(Serializer.Property.INDENT, "yes");
             out.setOutputFile(outputFile);
 
             XsltTransformer trans = exp.load();
 
             trans.setInitialContextNode(source);
             trans.setDestination(out);
             trans.transform();
 
         } catch (SaxonApiException ex) {
             
         }
         
               
            rdfDataUnit.extractFromFile(new File(config.getXmlFile()));
             ////////////
        
         
 
     }
 }
