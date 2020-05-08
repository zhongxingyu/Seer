 package ca.charland.tenideas.manage;
 
import ca.charland.activity.BaseDateAndTimeActivity;
 import ca.charland.activity.manage.BasePersonHomeActivityDataHolder;
 
 public class PersonHomeActivityDataHolder extends BasePersonHomeActivityDataHolder {
 
 	@Override
 	public Class<?> getNextAddClass() {
		return BaseDateAndTimeActivity.class;
 	}
 
 	@Override
 	public Class<?> getNextViewClass() {
 		return DateListOfPreviousEntriesActivity.class;
 	}
 
 	@Override
 	protected Class<?> getNextClass() {
 		return AllPeopleListActivity.class;
 	}
 }
