 package ch.bfh.evoting.voterapp.adapters;
 
 import java.text.DateFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 
 import android.app.Activity;
 import android.app.Fragment;
 import android.app.FragmentManager;
 import android.app.FragmentTransaction;
 import android.content.Context;
 import android.content.res.Configuration;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.TextView;
 import ch.bfh.evoting.voterapp.R;
 import ch.bfh.evoting.voterapp.entities.Option;
 import ch.bfh.evoting.voterapp.entities.Poll;
import ch.bfh.evoting.voterapp.fragment.ResultChartDialogFragment;
 import ch.bfh.evoting.voterapp.util.OptionsComparator;
 import ch.bfh.evoting.voterapp.util.ResultChartItem;
 import ch.bfh.evoting.voterapp.util.Separator;
 
 /**
  * List adapter showing a list of the options
  * 
  * @author von Bergen Philémon
  */
 public class ResultAdapter extends BaseAdapter {
 
 	private Context context;
 	private ArrayList<Object> data = new ArrayList<Object>();
 	private FragmentManager fm;
 	private FragmentTransaction ft;
 	private Fragment resultChartFragment;
 
 	public ResultAdapter(Context context, Poll poll) {
 		this.context = context;
 
 		int numberParticipants = poll.getNumberOfParticipants();
 
 		int numberCastVotes = 0;
 
 		for (Option option : poll.getOptions()) {
 			numberCastVotes += option.getVotes();
 		}
 
 		double participation = (double) numberCastVotes
 				/ (double) numberParticipants * 100;
 
 		List<Option> options = poll.getOptions();
 		Collections.sort(options, new OptionsComparator());
 
 		DateFormat dateFormat = android.text.format.DateFormat
 				.getDateFormat(context.getApplicationContext());
 
 		DateFormat timeFormat = android.text.format.DateFormat
 				.getTimeFormat(context.getApplicationContext());
 
 		Date startDate = new Date(poll.getStartTime());
 
 		data.add(new Separator(context.getResources().getString(
 				R.string.question)));
 		data.add(poll);
 		if (context.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
 			data.add(new Separator(context.getResources().getString(
 					R.string.result_chart)));
 			data.add(new ResultChartItem());
 		}
 		data.add(new Separator(context.getResources().getString(
 				R.string.options)));
 		data.addAll(options);
 		data.add(new Separator(context.getResources().getString(
 				R.string.poll_start_time)));
 		data.add(dateFormat.format(startDate) + " "
 				+ timeFormat.format(startDate));
 		data.add(new Separator(context.getResources().getString(
 				R.string.number_voters)));
 		data.add(numberParticipants + "");
 		data.add(new Separator(context.getResources().getString(
 				R.string.number_cast_votes)));
 		data.add(numberCastVotes + "");
 		data.add(new Separator(context.getResources().getString(
 				R.string.participation)));
 		data.add(String.format("%.1f", participation) + " %");
 
 	}
 
 	@Override
 	public View getView(final int position, View convertView, ViewGroup parent) {
 
 		View view;
 		LayoutInflater inflater = LayoutInflater.from(context);
 
 		if (this.getItem(position) instanceof Poll) {
 			view = inflater.inflate(R.layout.list_item_question, parent, false);
 			TextView tvSeparator = (TextView) view
 					.findViewById(R.id.textview_question);
 			tvSeparator.setText(((Poll) this.getItem(position)).getQuestion());
 		} else if (this.getItem(position) instanceof Option) {
 
 			Option option = (Option) this.getItem(position);
 
 			view = inflater.inflate(R.layout.list_item_option_result, parent,
 					false);
 			TextView tvOption = (TextView) view
 					.findViewById(R.id.textview_content);
 			TextView tvNumberVotes = (TextView) view
 					.findViewById(R.id.textview_number_votes);
 			TextView tvPercentage = (TextView) view
 					.findViewById(R.id.textview_percentage);
 
 			tvOption.setText(option.getText());
 			if (option.getVotes() == 1) {
 				tvNumberVotes.setText(option.getVotes()
 						+ " "
 						+ context.getResources().getString(
 								R.string.vote_singular));
 			} else {
 				tvNumberVotes.setText(option.getVotes()
 						+ " "
 						+ context.getResources()
 								.getString(R.string.vote_plural));
 			}
 			tvPercentage.setText(String.format("%.1f", option.getPercentage())
 					+ " %");
 		} else if (this.getItem(position) instanceof Separator) {
 			view = inflater
 					.inflate(R.layout.list_item_separator, parent, false);
 			TextView tvSeparator = (TextView) view
 					.findViewById(R.id.textview_separator);
 			tvSeparator.setText(((Separator) this.getItem(position)).getText());
 		} else if (this.getItem(position) instanceof ResultChartItem) {
 
 			fm = ((Activity) context).getFragmentManager();
			resultChartFragment = fm.findFragmentByTag("resultChart");
			if (resultChartFragment != null){
				ft = fm.beginTransaction();
				ft.remove(resultChartFragment);
				ft.commit();
			}
 			view = inflater.inflate(R.layout.list_item_chartfragment, parent,
 					false);
 		} else {
 			view = inflater.inflate(R.layout.list_item_string, parent, false);
 			TextView tvSeparator = (TextView) view
 					.findViewById(R.id.textview_content);
 			tvSeparator.setText(this.getItem(position).toString());
 		}
 		return view;
 	}
 
 	@Override
 	public int getCount() {
 		return data.size();
 	}
 
 	@Override
 	public Object getItem(int position) {
 		return data.get(position);
 	}
 
 	@Override
 	public long getItemId(int position) {
 		return position;
 	}
 }
