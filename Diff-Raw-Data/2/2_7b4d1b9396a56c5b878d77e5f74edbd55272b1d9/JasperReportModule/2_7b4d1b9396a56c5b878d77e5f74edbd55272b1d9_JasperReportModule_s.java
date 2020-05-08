 package br.com.wobr.reports.config;
 
 import net.sf.jasperreports.engine.JasperPrint;
 import br.com.wobr.reports.AbstractReportProcessor;
 import br.com.wobr.reports.ReportExporter;
 import br.com.wobr.reports.ReportProcessor;
 import br.com.wobr.reports.ReportProcessorFacade;
 import br.com.wobr.reports.jasper.JasperReportExporter;
 import br.com.wobr.reports.jasper.JasperReportProcessorForJava;
 import br.com.wobr.reports.jasper.JasperReportProcessorForModel;
 import br.com.wobr.reports.jasper.JasperReportProcessorForTemplate;
 
 import com.google.inject.AbstractModule;
 import com.google.inject.TypeLiteral;
 import com.google.inject.multibindings.Multibinder;
 import com.google.inject.name.Names;
 
 public class JasperReportModule extends AbstractModule
 {
 	@Override
 	protected void configure()
 	{
		Multibinder<AbstractReportProcessor> uriBinder = Multibinder.newSetBinder( binder(), AbstractReportProcessor.class, Names.named( "ForFacade" ) );
 
 		uriBinder.addBinding().to( JasperReportProcessorForTemplate.class );
 		uriBinder.addBinding().to( JasperReportProcessorForJava.class );
 		uriBinder.addBinding().to( JasperReportProcessorForModel.class );
 
 		bind( ReportProcessor.class ).to( ReportProcessorFacade.class );
 
 		bind( new TypeLiteral<ReportExporter<JasperPrint>>()
 		{
 		} ).to( JasperReportExporter.class );
 	}
 }
