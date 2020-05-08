 package name.kazennikov.annotations.patterns;
 
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import name.kazennikov.annotations.Annotation;
 
 import com.google.common.base.Objects;
 
 public class AnnotationMatchers {
 	
 	private AnnotationMatchers() {
 		
 	}
 	
 	public static abstract class BaseMatcher implements AnnotationMatcher {
 		String type;
 		
 		public BaseMatcher(String type) {
 			this.type = type;
 		}
 		
 		@Override
 		public String getType() {
 			return type;
 		}
 		
 		@Override
 		public int hashCode() {
 			return Objects.hashCode(this.getClass(), type);
 		}
 
 		@Override
 		public boolean equals(Object obj) {
 			if (this == obj)
 				return true;
 			
 			if (obj == null)
 				return false;
 			
 			if (!(obj instanceof BaseMatcher))
 				return false;
 			BaseMatcher other = (BaseMatcher) obj;
 			
 			if(!Objects.equal(this.type, other.type))
 				return false;
 
 			return true;
 		}
 	}
 		
 	/**
 	 * Matches the annotation type.
 	 * @author Anton Kazennikov
 	 *
 	 */
 	public static class TypeMatcher extends BaseMatcher {
 
 		/**
 		 * Construct a type matcher
 		 * @param type type to match, or null, to match any type
 		 */
 		public TypeMatcher(String type) {
 			super(type);
 		}
 
 		@Override
 		public boolean match(Annotation a) {
 			return type == null || a.getType().equals(type);
 		}
 		
 		@Override
 		public String toString() {
 			return String.format("{%s}", type);
 		}
 	}
 	
 	public static abstract class FeatureAccessor {
 		String name;
 		
 		public FeatureAccessor(String name) {
 			this.name = name;
 		}
 		
 		public String getName() {
 			return name;
 		}
 		
 		public abstract Object getValue(Annotation a);
 		
 		@Override
 		public int hashCode() {
 			return name.hashCode();
 		}
 		
 		@Override
 		public boolean equals(Object o) {
 			if(this == o)
 				return true;
 			
 			if(o == null)
 				return false;
 			
 			if(o.getClass() != this.getClass())
 				return false;
 			
 			FeatureAccessor fa = (FeatureAccessor) o;
 			return name.equals(fa.getName());
 		}
 		
 	}
 	
 	
 	
 	public static class SimpleFeatureAccessor extends FeatureAccessor {
 		public SimpleFeatureAccessor(String name) {
 			super(name);
 		}
 
 		@Override
 		public Object getValue(Annotation a) {
 			return a.getFeature(name);
 		}
 
 		@Override
 		public String getName() {
 			return name;
 		}
 		
 		@Override
 		public String toString() {
 			return "." + name;
 		}
 	}
 	
 	public static class StringMetaFeatureAccessor extends FeatureAccessor {
 		public StringMetaFeatureAccessor() {
 			super("string");
 		}
 
 		@Override
 		public Object getValue(Annotation a) {
 			return a.getText();
 		}
 		
 		@Override
 		public String toString() {
 			return "@string";
 		}
 	}
 
 	
 	
 	/**
 	 * Base full type/feature/value matcher. Used for matching:
 	 * 
 	 * type.feature OP value
 	 * @author Anton Kazennikov
 	 *
 	 */
 	public static abstract class BaseFeatureMatcher extends BaseMatcher {
 		FeatureAccessor fa;
 		Object value;
 		
 		public BaseFeatureMatcher(String type, FeatureAccessor fa, Object value) {
 			super(type);
 			this.fa = fa;
 			this.value = value;
 		}
 		
 		
 		public Object getValue(Annotation a) {
 			if(type != null && !a.getType().equals(type))
 				return null;
 			
 			return fa.getValue(a);
 		}
 		
 		
 		
 		
 		@Override
 		public boolean match(Annotation a) {
 			Object anValue = getValue(a);
 			if(anValue == null)
 				return false;
 			
 			return matchValue(anValue, value);
 		}
 		
 		public abstract boolean matchValue(Object annotationValue, Object matcherValue);
 
 
 		@Override
 		public int hashCode() {
 			return Objects.hashCode(this.getClass(), fa, type, value);
 		}
 
 		@Override
 		public boolean equals(Object obj) {
 			if (this == obj)
 				return true;
 			
 			if (obj == null)
 				return false;
 			
 			if (!(obj instanceof BaseFeatureMatcher))
 				return false;
 			
 			if(obj.getClass() != this.getClass())
 				return false;
 			
 			BaseFeatureMatcher other = (BaseFeatureMatcher) obj;
 			
 			if(!Objects.equal(this.type, other.type))
 				return false;
 			
 			if (!Objects.equal(this.fa, other.fa))
 				return false;
 			
 			if(!Objects.equal(this.value, other.value))
 				return false;
 
 			return true;
 		}
 	}
 	
 	public static class NegativeMatcher extends BaseFeatureMatcher {
 		BaseFeatureMatcher matcher;
 
 		public NegativeMatcher(BaseFeatureMatcher matcher) {
 			super(matcher.type, matcher.fa, matcher.value);
 			this.matcher = matcher;
 		}
 
 		@Override
 		public boolean matchValue(Object annotationValue, Object matcherValue) {
 			return !matcher.matchValue(annotationValue, matcherValue);
 		}
 		
 	}
 		
 		
 	public static class FeatureEqMatcher extends BaseFeatureMatcher {
 		public FeatureEqMatcher(String type, FeatureAccessor fa, Object value) {
 			super(type, fa, value);
 		}
 		
 		@Override
 		public String toString() {
 			return String.format("{%s%s == '%s'}", type, fa, value);
 		}
 		
 		@Override
 		public boolean matchValue(Object annotationObject, Object value) {
 			return value.equals(annotationObject);
 		}
 	}
 	
 	public static class FeatureRegexMatcher extends BaseFeatureMatcher {
 		Pattern p;
 		public FeatureRegexMatcher(String type, FeatureAccessor fa, Object value) {
 			super(type, fa, value);
 			p = Pattern.compile(value.toString());
 		}
 		
 		@Override
 		public String toString() {
 			return String.format("{%s%s ==~ '%s'}", type, fa, value);
 		}
 		

		
 		@Override
 		public boolean matchValue(Object annotationObject, Object value) {
 			String strAn = annotationObject.toString();
 			Matcher m = p.matcher(strAn);
			return m.matches();
 		}

		
 	}
 	
 	public static class FeatureContainsRegexMatcher extends BaseFeatureMatcher {
 		Pattern p;
 		public FeatureContainsRegexMatcher(String type, FeatureAccessor fa, Object value) {
 			super(type, fa, value);
 			p = Pattern.compile(value.toString());
 		}
 		
 		@Override
 		public String toString() {
 			return String.format("{%s%s =~ '%s'}", type, fa, value);
 		}
 		@Override
 		public boolean matchValue(Object annotationObject, Object value) {
 			String strAn = annotationObject.toString();
 			Matcher m = p.matcher(strAn);
			return m.find();
 		}
 	}
 
 
 	
 
 	/**
 	 * Complex matcher. Matches when all matchers matches.
 	 * Assume, that matchers are sorted by annotation types
 	 * 
 	 * @author Anton Kazennikov
 	 *
 	 */
 	public static class ANDMatcher implements AnnotationMatcher {
 		List<AnnotationMatcher> matchers;
 
 		public ANDMatcher(List<AnnotationMatcher> matchers) {
 			this.matchers = matchers;
 		}
 
 		@Override
 		public boolean match(Annotation a) {
 			for(AnnotationMatcher m : matchers) {
 				if(!m.match(a))
 					return false;
 			}
 			return true;
 		}
 
 		@Override
 		public String getType() {
 			return null;
 		}
 		
 		public List<AnnotationMatcher> getMatchers() {
 			return matchers;
 		}
 	}
 	
 	/**
 	 * NOT matcher wrapper. Intended to implement {!Token.foo == bar} matcher 
 	 * @author Anton Kazennikov
 	 *
 	 */
 	public static final class NOTAnnotationMatcher implements AnnotationMatcher {
 		AnnotationMatcher matcher;
 		
 		NOTAnnotationMatcher(AnnotationMatcher matcher) {
 			this.matcher = matcher;
 		}
 
 		@Override
 		public boolean match(Annotation a) {
 			return !matcher.match(a);
 		}
 
 		@Override
 		public String getType() {
 			return matcher.getType();
 		}
 		
 		public AnnotationMatcher getMatcher() {
 			return matcher;
 		}
 	}
 }
