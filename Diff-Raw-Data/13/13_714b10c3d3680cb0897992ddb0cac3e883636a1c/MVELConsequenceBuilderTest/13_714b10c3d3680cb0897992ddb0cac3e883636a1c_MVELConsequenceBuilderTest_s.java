 package org.drools.rule.builder.dialect.mvel;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 
 import junit.framework.TestCase;
 
 import org.drools.Cheese;
 import org.drools.RuleBase;
 import org.drools.RuleBaseFactory;
 import org.drools.WorkingMemory;
 import org.drools.base.ClassObjectType;
 import org.drools.base.DefaultKnowledgeHelper;
 import org.drools.common.AgendaItem;
 import org.drools.common.InternalFactHandle;
 import org.drools.common.PropagationContextImpl;
 import org.drools.compiler.DialectConfiguration;
import org.drools.compiler.DialectRegistry;
 import org.drools.compiler.PackageBuilder;
 import org.drools.compiler.PackageBuilderConfiguration;
 import org.drools.lang.descr.RuleDescr;
 import org.drools.reteoo.ReteTuple;
 import org.drools.rule.Declaration;
 import org.drools.rule.Package;
 import org.drools.rule.Pattern;
 import org.drools.spi.ObjectType;
 import org.drools.spi.PatternExtractor;
 
 public class MVELConsequenceBuilderTest extends TestCase {
 
     public void setUp() {
     }
 
     public void testSimpleExpression() throws Exception {
         final Package pkg = new Package( "pkg1" );
         final RuleDescr ruleDescr = new RuleDescr( "rule 1" );
         ruleDescr.setConsequence( "modify (cheese) {price = 5 }; retract (cheese)" );
 
         PackageBuilder pkgBuilder = new PackageBuilder( pkg );
         final PackageBuilderConfiguration conf = pkgBuilder.getPackageBuilderConfiguration();
         MVELDialect mvelDialect = ( MVELDialect ) ( (DialectConfiguration) conf.getDialectConfiguration( "mvel" ) ).getDialect();
 
         final InstrumentedBuildContent context = new InstrumentedBuildContent( conf,
                                                                                pkg,
                                                                                ruleDescr,
                                                                                conf.getDialectRegistry(),
                                                                                mvelDialect );
 
         final InstrumentedDeclarationScopeResolver declarationResolver = new InstrumentedDeclarationScopeResolver();
 
         final ObjectType cheeseObjeectType = new ClassObjectType( Cheese.class );
 
         final Pattern pattern = new Pattern( 0,
                                              cheeseObjeectType );
 
         final PatternExtractor extractor = new PatternExtractor( cheeseObjeectType );
 
         final Declaration declaration = new Declaration( "cheese",
                                                          extractor,
                                                          pattern );
         final Map map = new HashMap();
         map.put( "cheese",
                  declaration );
         declarationResolver.setDeclarations( map );
         context.setDeclarationResolver( declarationResolver );
 
         final MVELConsequenceBuilder builder = new MVELConsequenceBuilder();
         builder.build( context );
 
         final RuleBase ruleBase = RuleBaseFactory.newRuleBase();
         final WorkingMemory wm = ruleBase.newStatefulSession();
 
         final Cheese cheddar = new Cheese( "cheddar",
                                            10 );
         final InternalFactHandle f0 = (InternalFactHandle) wm.insert( cheddar );
         final ReteTuple tuple = new ReteTuple( f0 );
 
         final AgendaItem item = new AgendaItem( 0,
                                                 tuple,
                                                 10,
                                                 new PropagationContextImpl(1, 1, null, null),
                                                 context.getRule(),
                                                 null );
         final DefaultKnowledgeHelper kbHelper = new DefaultKnowledgeHelper( wm );
         kbHelper.setActivation( item );
         context.getRule().getConsequence().evaluate( kbHelper,
                                                      wm );
 
         assertEquals( 5,
                       cheddar.getPrice() );
     }
     
     public void testKnowledgeHelper() {
         
     }
     
     public void testImperativeCodeError() throws Exception {
         final Package pkg = new Package( "pkg1" );
         final RuleDescr ruleDescr = new RuleDescr( "rule 1" );
         ruleDescr.setConsequence( "if (cheese.price == 10) { cheese.price = 5; }" );
 
         Properties properties = new Properties();
         properties.setProperty( "drools.dialect.default",
                                 "mvel" );        
         PackageBuilderConfiguration cfg1 = new PackageBuilderConfiguration( properties );
         MVELDialect mvelDialect = ( MVELDialect) cfg1.getDefaultDialect();
         final PackageBuilderConfiguration conf = new PackageBuilderConfiguration();
 
         final InstrumentedBuildContent context = new InstrumentedBuildContent( conf,
                                                                                pkg,
                                                                                ruleDescr,
                                                                                conf.getDialectRegistry(),
                                                                                mvelDialect );
 
         final InstrumentedDeclarationScopeResolver declarationResolver = new InstrumentedDeclarationScopeResolver();
 
         final ObjectType cheeseObjeectType = new ClassObjectType( Cheese.class );
 
         final Pattern pattern = new Pattern( 0,
                                              cheeseObjeectType );
 
         final PatternExtractor extractor = new PatternExtractor( cheeseObjeectType );
 
         final Declaration declaration = new Declaration( "cheese",
                                                          extractor,
                                                          pattern );
         final Map map = new HashMap();
         map.put( "cheese",
                  declaration );
         declarationResolver.setDeclarations( map );
         context.setDeclarationResolver( declarationResolver );
 
         final MVELConsequenceBuilder builder = new MVELConsequenceBuilder();
         builder.build( context );
 
         final RuleBase ruleBase = RuleBaseFactory.newRuleBase();
         final WorkingMemory wm = ruleBase.newStatefulSession();
 
         final Cheese cheddar = new Cheese( "cheddar",
                                            10 );
         final InternalFactHandle f0 = (InternalFactHandle) wm.insert( cheddar );
         final ReteTuple tuple = new ReteTuple( f0 );
 
         final AgendaItem item = new AgendaItem( 0,
                                                 tuple,
                                                 10,
                                                 null,
                                                 context.getRule(),
                                                 null );
         final DefaultKnowledgeHelper kbHelper = new DefaultKnowledgeHelper( wm );
         kbHelper.setActivation( item );
         try {
         context.getRule().getConsequence().evaluate( kbHelper,
                                                      wm );
             fail( "should throw an exception, as 'if' is not allowed" );
         } catch ( Exception e) {
             
         }
         
         assertEquals( 10,
                       cheddar.getPrice() );
     }
 
     /**
      * Just like MVEL command line, we can allow expressions to span lines, with optional ";"
      * seperating expressions. If its needed a ";" can be thrown in, but if not, a new line is fine.
      * 
      * However, when in the middle of unbalanced brackets, a new line means nothing.
      * 
      * @throws Exception
      */
     public void testLineSpanOptionalSemis() throws Exception {
 
         String simpleEx = "foo\nbar\nbaz";
         MVELConsequenceBuilder cons = new MVELConsequenceBuilder();
         assertEquals( "foo;\nbar;\nbaz",
                       cons.delimitExpressions( simpleEx ) );
 
         String ex = "foo (\n bar \n)\nbar;\nyeah;\nman\nbaby";
         assertEquals( "foo (\n bar \n);\nbar;\nyeah;\nman;\nbaby",
                       cons.delimitExpressions( ex ) );
 
         ex = "foo {\n bar \n}\nbar;   \nyeah;\nman\nbaby";
         assertEquals( "foo {\n bar \n};\nbar;   \nyeah;\nman;\nbaby",
                       cons.delimitExpressions( ex ) );
 
         ex = "foo [\n bar \n]\nbar;  x\nyeah();\nman[42]\nbaby;ca chiga;\nend";
         assertEquals( "foo [\n bar \n];\nbar;  x;\nyeah();\nman[42];\nbaby;ca chiga;\nend",
                       cons.delimitExpressions( ex ) );
 
     }
 }
