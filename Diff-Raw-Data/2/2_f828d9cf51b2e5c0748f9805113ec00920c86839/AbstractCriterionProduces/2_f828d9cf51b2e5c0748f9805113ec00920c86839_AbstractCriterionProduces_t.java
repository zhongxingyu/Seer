 package br.jus.tre_pa.frameworkdemoiselle.query.filter.producers;
 
 import java.lang.reflect.Field;
 
 import javax.enterprise.inject.spi.InjectionPoint;
 import javax.inject.Inject;
 
 import br.gov.frameworkdemoiselle.util.Strings;
 import br.jus.tre_pa.frameworkdemoiselle.query.filter.annotations.Attribute;
 import br.jus.tre_pa.frameworkdemoiselle.query.filter.context.CriteriaContext;
 import br.jus.tre_pa.frameworkdemoiselle.query.filter.internal.AbstractQueryCriterion;
 
 public class AbstractCriterionProduces {
 
 	@Inject
 	private CriteriaContext context;
 
 	protected String getFieldName(InjectionPoint ip) {
 		Field field = (Field) ip.getMember();
		if (!field.isAnnotationPresent(Attribute.class) || Strings.isEmpty(field.getAnnotation(Attribute.class).name())) {
 			return field.getName();
 		}
 		return field.getAnnotation(Attribute.class).name();
 	}
 
 	protected <X> void addCriterionToContext(InjectionPoint ip, AbstractQueryCriterion criterion) {
 		Class<?> filterClass = ip.getMember().getDeclaringClass();
 		context.put(filterClass, criterion);
 	}
 
 }
