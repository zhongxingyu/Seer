 package uk.ac.susx.mlcl.erl.tac.eval;
 
 import com.google.common.base.Function;
 import com.google.common.base.Predicate;
 import com.google.common.collect.ImmutableMap;
 import com.google.common.io.Resources;
 import nu.xom.ParsingException;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import uk.ac.susx.mlcl.erl.lib.Comparators;
 import uk.ac.susx.mlcl.erl.reduce.Reducers;
 import uk.ac.susx.mlcl.erl.tac.io.LinkIO;
 import uk.ac.susx.mlcl.erl.tac.queries.Link;
 import uk.ac.susx.mlcl.erl.test.AbstractTest;
 
 import javax.annotation.Nullable;
 import java.io.IOException;
 import java.net.URL;
 import java.util.Comparator;
 import java.util.List;
 import java.util.Locale;
 
 /**
  * Created with IntelliJ IDEA.
  * User: hiam20
  * Date: 02/08/2013
  * Time: 14:33
  * To change this template use File | Settings | File Templates.
  */
 public class EvaluationTest extends AbstractTest {
 
     List<Link> actual;
     List<Link> predicted;
 
     @Before
     public void foo() throws IOException, ParsingException {
         final URL actualUrl = Resources.getResource(LinkIO.class, "tac_2012_kbp_english_evaluation_entity_linking_query_types.tab");
         final URL predictedUrl = Resources.getResource(this.getClass(), "tac_2012_kbp_english_evaluation_entity_linking_predicted_exactmatch.tab");
 
         actual = LinkIO.detectFormat(actualUrl).readAll(actualUrl);
         predicted = LinkIO.detectFormat(predictedUrl).readAll(predictedUrl);
     }
 
     /**
      * Run a link evaluation on exact node ID matches; i.e for a prediction to be correct the node ID must be
      * exactly identical to the actual value. This is not a fair evaluation since, amount othe things, the NIL ids
      * are likely to be incremented differently.
      */
     @Test
     public void testNodeId() {
 
         // define the notion of link equality, in this case node id
         final Comparator<Link> linkComparator = Comparators.mapped(Link.GetEntityNodeId.INSTANCE);
 
         // when displaying results print just the node id
         final Function<Link, String> labelFormatter = Link.GetEntityNodeId.INSTANCE;
 
         final Evaluation evaluation = new Evaluation(actual, predicted, linkComparator, labelFormatter);
         Assert.assertNotNull("evaluation", evaluation);
         evaluation.getConfusionMatrix().appendStats(System.out, Locale.getDefault());
 
 //        Assert.fail();
     }
 
     /**
      * Evaluate whole well the prediction performed in terms of entities found vs. not found (nil).
      * <p/>
      * In this method we set up the full evaluation as before, then map the confusion matrix to binary matrix.
      */
     @Test
     public void testEntityVsNil_Method1() throws NoSuchMethodException {
 
         final Comparator<Link> linkComparator = Comparators.mapped(Link.GetEntityNodeId.INSTANCE);
         final Function<Link, String> labelFormatter = Link.GetEntityNodeId.INSTANCE;
 
         final Evaluation evaluation = new Evaluation(actual, predicted, linkComparator, labelFormatter);
         Assert.assertNotNull("evaluation", evaluation);
 //        System.out.println(evaluation);
 
         final ConfusionMatrix fullMatrix = evaluation.getConfusionMatrix();
 
 //        System.out.println(fullMatrix.getAccuracy());
 
         BinaryConfusionMatrix binMatrix = fullMatrix.mapAllVersus(new Predicate<Link>() {
             @Override
             public boolean apply(@Nullable Link input) {
                 return !input.getEntityNodeId().startsWith("NIL");
             }
        }, "Entity", "Nil", Reducers.sum());
 
         Assert.assertNotNull("binMatrix", binMatrix);
 //        System.out.println(binMatrix.getStatsString());
         binMatrix.appendStatsFor(binMatrix.getPositiveLabel(), System.out, Locale.getDefault());
         binMatrix.appendStatsFor(binMatrix.getNegativeLabel(), System.out, Locale.getDefault());
 
 //        Assert.fail();
     }
 
     /**
      * Evaluate whole well the prediction performed in terms of entities found vs. not found (nil).
      * <p/>
      * In this method the binary matrix is computer directly from the evaluation result by supplying the
      * appropriate comparator and formatter.
      */
     @Test
     public void testEntVsNil_Method2() {
 
         // Note the link comparator here performs two functions. First it matches entities and nils by stripping
         // the numeric suffix. Secondly it insures that, when displaying results, Entity is the first (positive)
         // element, and that NIL is the second (negative) element.
         final Comparator<Link> linkComparator =
                 Comparators.mapped(Link.GetEntityIdPrefix.INSTANCE,
                         Comparators.mapped(ImmutableMap.of(
                                 Link.GetEntityIdPrefix.NIL_PREFIX_RESULT, 1,
                                 Link.GetEntityIdPrefix.ENTITY_PREFIX_RESULT, 0)));
 
 //
 //        new Comparator<Link>() {
 //
 //            @Override
 //            public int compare(Link o1, Link o2) {
 //                return Integer.compare(
 //                        o1.getEntityNodeId().startsWith("NIL") ? 1 : 0,
 //                        o2.getEntityNodeId().startsWith("NIL") ? 1 : 0);
 //            }
 //        };
 
 
         final Function<Link, String> labelFormatter = Link.GetEntityIdPrefix.INSTANCE;
 
         Evaluation evaluation = new Evaluation(actual, predicted, linkComparator, labelFormatter);
         Assert.assertNotNull("evaluation", evaluation);
 //        System.out.println(evaluation);
 
         ConfusionMatrix mat = evaluation.getConfusionMatrix();
         Assert.assertNotNull("mat", mat);
         Assert.assertEquals(BinaryConfusionMatrix.class, mat.getClass());
         System.out.println(mat);
 
 
 //        Assert.fail();
     }
 
     /**
      * Evaluate entity type predictions (PER,GPE,ORG,UKN) against each other.
      */
     @Test
     public void testEntTypes() {
 
         final Comparator<Link> linkComparator = Comparators.mapped(Link.GetEntityType.INSTANCE);
         final Function<Link, String> labelFormatter = Link.GetEntityTypeString.INSTANCE;
 
         final Evaluation<Link> evaluation = new Evaluation(actual, predicted, linkComparator, labelFormatter);
 
         ConfusionMatrix<Link> fourWayMatrix = evaluation.getConfusionMatrix();
         Assert.assertEquals(4, fourWayMatrix.getLabels().size());
 
         fourWayMatrix.appendTable(System.out, Locale.getDefault());
         fourWayMatrix.appendStats(System.out, Locale.getDefault());
 
         // For each entity type we can also create a binary confusion matrix show the all-vs-one scores.
         for (final Link label : fourWayMatrix.getLabels()) {
             ConfusionMatrix twoWayMatrix = fourWayMatrix.mapAllVersus(new Predicate<Link>() {
                 @Override
                 public boolean apply(@Nullable Link input) {
                     return input.getEntityType().equals(label.getEntityType());
                 }
            }, label.getEntityType().name(), "Other", Reducers.sum());
 
             System.out.println(label.getEntityType().name());
             twoWayMatrix.appendTable(System.out, Locale.getDefault());
             twoWayMatrix.appendStats(System.out, Locale.getDefault());
         }
 
 
 //        Assert.fail();
     }
 
 
 }
