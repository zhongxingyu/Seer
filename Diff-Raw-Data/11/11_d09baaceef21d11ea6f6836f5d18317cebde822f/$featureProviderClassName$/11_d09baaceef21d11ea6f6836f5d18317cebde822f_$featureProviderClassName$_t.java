 package $packageName$.diagram;
 
 import org.eclipse.graphiti.dt.IDiagramTypeProvider;
 import org.eclipse.graphiti.features.IAddFeature;
 import org.eclipse.graphiti.features.ICreateConnectionFeature;
 import org.eclipse.graphiti.features.context.IAddConnectionContext;
 import org.eclipse.graphiti.features.context.IAddContext;
 import org.eclipse.graphiti.pattern.DefaultFeatureProviderWithPatterns;
 
 import $packageName$.features.Add$connectionDomainObjectClassNameShort$ConnectionFeature;
 import $packageName$.features.Create$connectionDomainObjectClassNameShort$ConnectionFeature;
 import $packageName$.patterns.$shapeDomainObjectClassNameShort$Pattern;
 
 
 public class $featureProviderClassName$ extends DefaultFeatureProviderWithPatterns {
 
 	public $featureProviderClassName$(IDiagramTypeProvider dtp) {
 		super(dtp);
		addPattern(new $shapeDomainObjectClassNameShort$Pattern());
 	}
 
 	@Override
 	public ICreateConnectionFeature[] getCreateConnectionFeatures() {
 		return new ICreateConnectionFeature[] {new Create$connectionDomainObjectClassNameShort$ConnectionFeature(this)};
 	}
 	
 	@Override
 	public IAddFeature getAddFeature(IAddContext context) {
 		// TODO: check for right domain object instances below
 		if (context instanceof IAddConnectionContext /* && context.getNewObject() instanceof $connectionDomainObjectClassNameShort$ */) {
 			return new Add$connectionDomainObjectClassNameShort$ConnectionFeature(this);
 		}
 		return super.getAddFeature(context);
 	}
 }
