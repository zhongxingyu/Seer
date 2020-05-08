 package woodsie.avalanche.checklist;
 
 import static woodsie.avalanche.section.CollapsibleSection.State.OPEN;
 
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import woodsie.avalanche.R;
 import woodsie.avalanche.section.CollapsibleSection;
 import woodsie.avalanche.section.CollapsibleSectionListener;
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.CheckBox;
 import android.widget.ScrollView;
 
 public class Checklist3x3Activity extends Activity implements OnClickListener {
 
 	private Map<String, CollapsibleSection> sectionMap;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.checklist_3x3);
 
 		sectionMap = new LinkedHashMap<String, CollapsibleSection>();
 
 		sectionMap.put("description", new CollapsibleSection(this, R.id.descriptionHeading, R.id.descriptionArrow, R.id.descriptionLayout));
 		sectionMap.put("regional", new CollapsibleSection(this, R.id.regionalHeading, R.id.regionalArrow, R.id.regionalLayout));
 		sectionMap.put("local", new CollapsibleSection(this, R.id.localHeading, R.id.localArrow, R.id.localLayout));
 		sectionMap.put("zonal", new CollapsibleSection(this, R.id.zonalHeading, R.id.zonalArrow, R.id.zonalLayout));
 
 		CollapsibleSectionListener sectionListener = new CollapsibleSectionListener(sectionMap, (ScrollView) findViewById(R.id.checklistScroller));
 
 		for (Entry<String, CollapsibleSection> entry : sectionMap.entrySet()) {
			CollapsibleSection value = entry.getValue();
 			if (savedInstanceState != null && savedInstanceState.getBoolean(entry.getKey())) {
				value.getSectionLayout().setVisibility(View.VISIBLE);
 			}
			value.getHeading().setOnClickListener(sectionListener);
			value.getArrowImage().setOnClickListener(sectionListener);
 		}
 
 		findViewById(R.id.checklist3x3ResetTop).setOnClickListener(this);
 		findViewById(R.id.checklist3x3ResetBottom).setOnClickListener(this);
 	}
 
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 
 		for (Entry<String, CollapsibleSection> entry : sectionMap.entrySet()) {
 			outState.putBoolean(entry.getKey(), entry.getValue().getState() == OPEN);
 		}
 	}
 
 	public void onClick(View view) {
 
 		for (CollapsibleSection section : sectionMap.values()) {
 			section.close();
 		}
 
 		recursiveReset(findViewById(R.id.checklist3x3Form));
 
 		((ScrollView) findViewById(R.id.checklistScroller)).scrollTo(0, 0);
 	}
 
 	private void recursiveReset(View view) {
 
 		if (view instanceof ViewGroup) {
 			ViewGroup group = (ViewGroup) view;
 			for (int i = 0; i < group.getChildCount(); i++) {
 				recursiveReset(group.getChildAt(i));
 			}
 		} else if (view instanceof CheckBox) {
 			((CheckBox) view).setChecked(false);
 		}
 	}
 
 }
