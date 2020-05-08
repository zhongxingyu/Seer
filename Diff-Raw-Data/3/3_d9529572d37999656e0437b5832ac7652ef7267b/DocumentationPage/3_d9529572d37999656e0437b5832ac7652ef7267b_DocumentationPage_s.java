 package net.micwin.elysium.view.documentation;
 
 import java.text.NumberFormat;
 
 import org.apache.wicket.markup.html.basic.Label;
 
 import net.micwin.elysium.bpo.NaniteBPO;
 import net.micwin.elysium.entities.NaniteGroup;
 import net.micwin.elysium.entities.NaniteGroup.State;
 import net.micwin.elysium.view.BasePage;
 
 public class DocumentationPage extends BasePage {
 
 	public DocumentationPage() {
 		super(false);
 	}
 
 	@Override
 	protected void onInitialize() {
 		super.onInitialize();
 		addToContentBody(new Label("entrenchedSignatureFactor", NumberFormat.getNumberInstance().format(
 						State.ENTRENCHED.getSignatureFactor())));
 		addToContentBody(new Label("entrenchedDefenseFactor", NumberFormat.getNumberInstance().format(
 						State.ENTRENCHED.getReceivingDamageFactor())));
 		addToContentBody(new Label("entrenchedSensorsFactor", NumberFormat.getNumberInstance().format(
 						State.ENTRENCHED.getSensorFactor())));
 		addToContentBody(new Label("entrenchedCounterstrikeFactor", NumberFormat.getNumberInstance().format(
 						State.ENTRENCHED.getCounterStrikeDamageFactor())));
 		int level = getAvatar() != null ? getAvatar().getLevel() : 5;
 		addToContentBody(new Label("level", NumberFormat.getIntegerInstance().format(level)));
 
 		addToContentBody(new Label("entrenchingDuration", NumberFormat.getIntegerInstance().format(level * 5)));
 		addToContentBody(new Label("attackFactor1", NumberFormat.getNumberInstance().format(
 						new NaniteBPO().computeNumberBasedEfficiencyFactor(1))));
 		addToContentBody(new Label("attackFactor10", NumberFormat.getNumberInstance().format(
 						new NaniteBPO().computeNumberBasedEfficiencyFactor(10))));
 		addToContentBody(new Label("attackFactor100", NumberFormat.getNumberInstance().format(
 						new NaniteBPO().computeNumberBasedEfficiencyFactor(100))));
 		addToContentBody(new Label("attackFactor1000", NumberFormat.getNumberInstance().format(
 						new NaniteBPO().computeNumberBasedEfficiencyFactor(1000))));
 		addToContentBody(new Label("attackFactor10000", NumberFormat.getNumberInstance().format(
 						new NaniteBPO().computeNumberBasedEfficiencyFactor(10000))));
 		addToContentBody(new Label("attackFactor1Mio", NumberFormat.getNumberInstance().format(
 						new NaniteBPO().computeNumberBasedEfficiencyFactor(1000000))));
 		addToContentBody(new Label("attackFactorMax", NumberFormat.getNumberInstance().format(
 						new NaniteBPO().computeNumberBasedEfficiencyFactor(Integer.MAX_VALUE))));
 
		addToContentBody(new Label("maxNanitesGroupSize", NumberFormat.getNumberInstance().format(
						new NaniteBPO().computeNumberBasedEfficiencyFactor(NaniteGroup.MAX_NANITES_COUNT))));
 
 	}
 }
