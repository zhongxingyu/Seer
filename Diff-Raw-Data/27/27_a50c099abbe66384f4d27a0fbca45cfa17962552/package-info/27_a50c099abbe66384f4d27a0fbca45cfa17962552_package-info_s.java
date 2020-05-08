 /**
  * <p>Contains all functionality and classes interpret the AIML language. For
  * the end user, these particular classes may be considered the API to use the
  * aiml package:
  * <dl>
  * <dt>{@link aiml.classifier.Classifier aiml.classifier.Classifier}</dt>
 * <dd>The matching functionality</dd>
  * <dt>{@link aiml.classifier.Path aiml.classifier.Path}</dt>
  * <dd>Represents the path, or "category context". Create objects of this class to
  * set the conditions for a particular match.</dd>
  * <dt>{@link aiml.context.ContextInfo aiml.context.ContextInfo}</dt>
  * <dd>Maintans information about all known contexts. Encapsulates the "state
  * of the world". You must initialize this structure with enough context sources
 * before adding paths to the PatternMatcher!
 * </dd>
  * <dt>{@link aiml.context.Context aiml.context.Context}</dt>
 * <dd>Encapsulates a particular state used in matching. Inherit this to provide
 * sources of data for your matching environment.
 * </dd>
  * </dl>
  * </p>
  * <p>Please be aware that the organization of this library is not yet final, and
  * that in future releases, the package structure and implementation details may
  * change. The basic layout and usage should not.</p>
  */
 package aiml;
