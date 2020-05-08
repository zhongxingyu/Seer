 package net.eledge.android.eu.europeana.search.model.record.enums;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import net.eledge.android.eu.europeana.EuropeanaApplication;
 import net.eledge.android.eu.europeana.R;
 import net.eledge.android.eu.europeana.search.model.record.Record;
 import net.eledge.android.eu.europeana.search.model.record.abstracts.RecordView;
 import net.eledge.android.toolkit.StringArrayUtils;
 
 import org.apache.commons.lang.StringUtils;
 
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.TextView;
 
 public enum RecordDetails implements RecordView {
 	
 	TITLE {
 		@Override
 		public boolean isVisible(Record record) {
 			return true;
 		}
 		@Override
 		public View getView(Record record, ViewGroup parent, LayoutInflater inflater, EuropeanaApplication application) {
 			View view = inflater.inflate(R.layout.listitem_record_title, parent, false);
 			TextView text1 = (TextView) view.findViewById(android.R.id.text1);
 			text1.setText(StringUtils.join(record.title, ";"));
 			TextView text2 = (TextView) view.findViewById(android.R.id.text2);
 			text2.setTypeface(application.getEuropeanaFont());
 			text2.setText(record.type.icon);
 			return view;
 		}
 	},
 	DCCREATOR {
 		@Override
 		public boolean isVisible(Record record) {
 			return StringArrayUtils.isNotBlank(record.dcCreator);
 		}
 		@Override
 		public View getView(Record record, ViewGroup parent, LayoutInflater inflater, EuropeanaApplication application) {
 			return drawDetailView(R.string.record_field_dc_creator, record.dcCreator, parent, inflater);
 		}
 	},
 	DCTYPE {
 		@Override
 		public boolean isVisible(Record record) {
 			return StringArrayUtils.isNotBlank(record.dcType);
 		}
 		@Override
 		public View getView(Record record, ViewGroup parent, LayoutInflater inflater, EuropeanaApplication application) {
 			return drawDetailView(R.string.record_field_dc_type, record.dcType, parent, inflater);
 		}
 	},
 	DCSUBJECT {
 		@Override
 		public boolean isVisible(Record record) {
 			return StringArrayUtils.isNotBlank(record.dcSubject);
 		}
 		@Override
 		public View getView(Record record, ViewGroup parent, LayoutInflater inflater, EuropeanaApplication application) {
 			return drawDetailView(R.string.record_field_dc_subject, record.dcSubject, parent, inflater);
 		}
 	},
 	DCIDENTIFIER {
 		@Override
 		public boolean isVisible(Record record) {
 			return StringArrayUtils.isNotBlank(record.dcIdentifier);
 		}
 		@Override
 		public View getView(Record record, ViewGroup parent, LayoutInflater inflater, EuropeanaApplication application) {
 			return drawDetailView(R.string.record_field_dc_identifier, record.dcIdentifier, parent, inflater);
 		}
 	},
 	EDMDATAPROVIDER {
 		@Override
 		public boolean isVisible(Record record) {
 			return StringArrayUtils.isNotBlank(record.edmDataProvider);
 		}
 		@Override
 		public View getView(Record record, ViewGroup parent, LayoutInflater inflater, EuropeanaApplication application) {
 			return drawDetailView(R.string.record_field_edm_dataprovider, record.edmDataProvider, parent, inflater);
 		}
 	},
 	EDMPROVIDER {
 		@Override
 		public boolean isVisible(Record record) {
 			return StringArrayUtils.isNotBlank(record.edmProvider);
 		}
 		@Override
 		public View getView(Record record, ViewGroup parent, LayoutInflater inflater, EuropeanaApplication application) {
 			return drawDetailView(R.string.record_field_edm_provider, record.edmProvider, parent, inflater);
 		}
 	};
 	
 	protected View drawDetailView(int titleResId, String[] values, ViewGroup parent, LayoutInflater inflater) {
 		return drawDetailView(titleResId, StringUtils.join(values, ";"), parent, inflater);
 	}
 	
 	protected View drawDetailView(int titleResId, String value, ViewGroup parent, LayoutInflater inflater) {
 		View view = inflater.inflate(R.layout.listitem_record_detail, parent, false);
 		TextView textTitle = (TextView) view.findViewById(android.R.id.text1);
 		TextView textValue = (TextView) view.findViewById(android.R.id.text2);
 		textTitle.setText(titleResId);
 		textValue.setText(value);
 		return view;
 	}
 	
 	public static List<RecordDetails> getVisibles(Record record) {
 		List<RecordDetails> list = new ArrayList<RecordDetails>();
 		if (record != null) {
 			for (RecordDetails detail: RecordDetails.values()) {
 				if (detail.isVisible(record)) {
 					list.add(detail);
 				}
 			}
 		}
 		return list;
 	}
 	
 }
