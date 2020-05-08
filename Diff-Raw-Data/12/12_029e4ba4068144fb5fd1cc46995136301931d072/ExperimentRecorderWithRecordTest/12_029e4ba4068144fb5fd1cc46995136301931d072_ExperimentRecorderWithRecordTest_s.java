 package de.hub.clickwatch.recorder.test;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.List;
 
 import junit.framework.Assert;
 
 import org.junit.Ignore;
 import org.junit.Test;
 
 import com.google.common.base.Function;
 import com.google.common.collect.Lists;
 import com.google.inject.AbstractModule;
 import com.google.inject.Module;
 
 import de.hub.clickcontrol.IClickSocket;
 import de.hub.clickwatch.connection.adapter.IValueAdapter;
 import de.hub.clickwatch.connection.adapter.StringValueAdapter;
 import de.hub.clickwatch.model.Handler;
 import de.hub.clickwatch.model.Node;
 import de.hub.clickwatch.recorder.ClickSocketPlayer;
 import de.hub.clickwatch.recorder.ClickSocketPlayerSocketImpl;
 
 public class ExperimentRecorderWithRecordTest extends AbstractDBTest {
 
 	@Override
 	protected Module[] getAdditionalModules() {
 		String record = "../../ui/de.hub.clickwatch.ui/resources/records/record_11-06-23.clickwatchmodel";
 		AbstractModule module = new ClickSocketPlayer.PlayerModule(record,
 				false);
 		List<Module> result = new ArrayList<Module>();
 		result.addAll(Arrays.asList(super.getAdditionalModules()));
 		result.add(module);
 		return result.toArray(new Module[] {});
 	}
 	
 	@Override
 	protected int getLogLevel() {
 		return 4;
 	}
 
 	@Override
 	protected Class<? extends IValueAdapter> getValueAdapterClass() {
 		return StringValueAdapter.class;
 	}
 
 	@Override
 	protected Class<? extends IClickSocket> getClickSocketClass() {
 		return ClickSocketPlayerSocketImpl.class;
 	}
 
 	@Override
 	protected void adapterTestSetUp() {
 		// empty
 		// this disables the adapterTest setup part. Actually you don't this
 		// test to be an adapter test, but since multiple inheritance is not
 		// supported, we use this trick.
 	}
 
 	@Ignore("work in progress")
 	@Test
 	public void testWithRecord() throws Exception {
 		performTest(new String[] { "localhost", "seismo171.testbed",
 				"seismo161.testbed", "seismo191.testbed", "seismo160.testbed",
 				"seismo185.testbed", "seismo196.testbed", "seismo172.testbed" });
 	}
 	
 	@Override
 	protected Collection<String> handlerNamesOfNode(Node node) {
 		return Lists.transform(node.getAllHandlers(), new Function<Handler, String>() {
 			  public String apply(Handler from) {
 				  return from.getQualifiedName();
 			  }
 		});
 	}
 	
 	protected void assertValue(Handler handler) {
 		Assert.assertTrue(true);
 	}
 
 	@Override
	protected int getUpdateIntertval() {
 		return 10000;
 	}
 }
