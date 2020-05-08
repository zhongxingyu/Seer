 package ch.almana.android.unibas.perssearch.view;
 
 import java.util.List;
 
 import android.content.Context;
 import android.content.Intent;
 import android.net.Uri;
 import android.text.TextUtils.TruncateAt;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.BaseAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 import ch.almana.android.unibas.perssearch.R;
 import ch.almana.android.unibas.perssearch.helper.Logger;
 import ch.almana.android.unibas.perssearch.model.Person;
 import ch.almana.android.unibas.perssearch.model.Person.FieldTypes;
 
 class PersonDetailAdapter extends BaseAdapter implements AdapterView.OnItemClickListener {
 
 	private final PersonDetailsActivity personDetailsActivity;
 	private final Person person;
 	private final LayoutInflater inflater;
 	private List<FieldTypes> fields;
 
 	public PersonDetailAdapter(PersonDetailsActivity personDetailsActivity, Person person) {
 		this.personDetailsActivity = personDetailsActivity;
 		this.person = person;
 		fields = person.getNonblankFields();
 		inflater = (LayoutInflater) personDetailsActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 	}
 
 	public int getCount() {
 		return fields.size();
 	}
 
 	public Object getItem(int position) {
 		return position;
 	}
 
 	public long getItemId(int position) {
 		return position;
 	}
 
 	public View getView(int position, View convertView, ViewGroup parent) {
 		// View view = (convertView != null) ? convertView : createView(parent);
 		View view = createView(parent);
 		bindView(view, fields.get(position));
 		return view;
 	}
 
 	private View createView(ViewGroup parent) {
 		return inflater.inflate(R.layout.person_detail_list_item, parent, false);
 	}
 
 
 	private void bindView(View view, FieldTypes field) {
 
 		int labelResId;
 		String value;
 		int imageResId = -1;
 		int count = 1;
 		TextView tvLabel = (TextView) view.findViewById(R.id.tvLabel);
 		TextView tvValue = (TextView) view.findViewById(R.id.tvValue);
 		ImageView iwAction = ((ImageView) view.findViewById(R.id.iwAction));
 
 		switch (field) {
 		case MAIL_WORK:
 			labelResId = R.string.email_work;
 			value = person.getEmailWork();
 			imageResId = android.R.drawable.sym_action_email;
 			break;
 		case MAIL_HOME:
 			labelResId = R.string.email_home;
 			value = person.getEmailHome();
 			imageResId = android.R.drawable.sym_action_email;
 			break;
 		case MAIL_INSTITUTE:
 			labelResId = R.string.email_institute;
 			value = person.getEmailInstitute();
 			imageResId = android.R.drawable.sym_action_email;
 			break;
 		case PHONE_WORK:
 			labelResId = R.string.phone_work;
 			value = person.getPhoneWork();
 			imageResId = android.R.drawable.sym_action_call;
 			break;
 		case PHONE_MOBILE:
 			labelResId = R.string.phone_mobile;
 			value = person.getPhoneMobile();
 			imageResId = android.R.drawable.sym_action_call;
 			break;
 		case PHONE_HOME:
 			labelResId = R.string.phone_home;
 			value = person.getPhoneHome();
 			imageResId = android.R.drawable.sym_action_call;
 			break;
 		case ADDRESS_WORK:
 			labelResId = R.string.address_work;
 			value = person.getAddressWork();
 			// buttonImageResId = android.R.drawable.ic_dialog_map;
 			int idx = 0;
 			while ((idx = value.indexOf("\n", idx + 1)) != -1) {
 				count++;
 			}
 			break;
 		case ADDRESS_HOME:
 			labelResId = R.string.address_home;
 			value = person.getAddressHome();
 			int idx2 = 0;
 			while ((idx2 = value.indexOf("\n", idx2 + 1)) != -1) {
 				count++;
 			}
 			break;
 		case FAX_WORK:
 			labelResId = R.string.fax_work;
 			value = person.getFaxWork();
 			break;
 		case FAX_HOME:
 			labelResId = R.string.fax_home;
 			value = person.getFaxHome();
 			break;
 		case WEBPAGE:
 			labelResId = R.string.website;
 			// buttonImageResId = android.R.drawable.ic_menu_mapmode;
 			value = person.getWebpage();
 			break;
 
 		default:
 			labelResId = R.string.unknown;
 			value = "";
 			break;
 		}
 
 		tvLabel.setText(labelResId);
 		tvValue.setText(value);
 		int height = personDetailsActivity.getTextHeight()+2;
 		tvValue.setMinimumHeight(height * count);
 		tvValue.setEllipsize(TruncateAt.END);
 		view.setMinimumHeight(height * (count + 2));
 		if (imageResId != -1) {
 			iwAction.setVisibility(View.VISIBLE);
 			iwAction.setBackgroundDrawable(personDetailsActivity.getResources().getDrawable(imageResId));
 		} else {
 			iwAction.setVisibility(View.INVISIBLE);
 		}
 
 	}
 
 	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 		Intent intent;
 		switch (fields.get(position)) {
 		case MAIL_WORK:
 			intent = new Intent(Intent.ACTION_SEND);
 			intent.putExtra(Intent.EXTRA_EMAIL, new String[] { person.getEmailWork() });
 			intent.setType("plain/text");
 			break;
 		case MAIL_HOME:
 			intent = new Intent(Intent.ACTION_SEND);
 			intent.putExtra(Intent.EXTRA_EMAIL, new String[] { person.getEmailHome() });
 			intent.setType("plain/text");
 			break;
 		case MAIL_INSTITUTE:
 			intent = new Intent(Intent.ACTION_SEND);
 			intent.putExtra(Intent.EXTRA_EMAIL, new String[] { person.getEmailInstitute() });
 			intent.setType("plain/text");
 			break;
 		case PHONE_WORK:
 			intent = new Intent(Intent.ACTION_DEFAULT);
 			intent.setData(Uri.parse("tel:" + person.getPhoneWork()));
 			break;
 		case PHONE_MOBILE:
 			intent = new Intent(Intent.ACTION_DEFAULT);
 			intent.setData(Uri.parse("tel:" + person.getPhoneMobile()));
 			break;
 		case PHONE_HOME:
 			intent = new Intent(Intent.ACTION_DEFAULT);
 			intent.setData(Uri.parse("tel:" + person.getPhoneHome()));
 			break;
 		case ADDRESS_WORK:
 			intent = new Intent(Intent.ACTION_VIEW);
 			intent.setData(Uri.parse("geo:0,0?q=" + person.getStreetCityWork()));
 			break;
 		case ADDRESS_HOME:
 			intent = new Intent(Intent.ACTION_VIEW);
 			intent.setData(Uri.parse("geo:0,0?q=" + person.getAddressHome()));
 			break;
 		case WEBPAGE:
 			intent = new Intent(Intent.ACTION_VIEW);
 			intent.setData(Uri.parse(person.getWebpage()));
 			break;
 
 		default:
 			intent = null;
 			break;
 		}
 		if (intent != null) {
 			try {
 				personDetailsActivity.startActivity(intent);
 			} catch (Exception e) {
 				Logger.w("Could not start field activity", e);
 				// FIXME: toast
 			}
 		}
 
 	}
 }
