 package de.wak_sh.client.backend.adapters;
 
 import java.util.Locale;
 
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
 import de.wak_sh.client.backend.service.ModuleService;
 import de.wak_sh.client.fragments.SemesterFragment;
 
public class ModulePagerAdapter extends FragmentStatePagerAdapter {
 	private ModuleService moduleService;
 
 	public ModulePagerAdapter(FragmentManager fm) {
 		super(fm);
 	}
 
 	@Override
 	public Fragment getItem(int position) {
 		SemesterFragment fragment = new SemesterFragment();
 		Bundle bundle = new Bundle();
 		bundle.putSerializable("grades", moduleService.getGrades(position + 1));
 		bundle.putFloat("average", moduleService.getAverageGrade(position + 1));
 		fragment.setArguments(bundle);
 		return fragment;
 	}
 
 	@Override
 	public int getCount() {
 		if (moduleService == null) {
 			return 0;
 		}
 		return moduleService.countSemesters();
 	}
 
 	public void setModuleService(ModuleService moduleService) {
 		this.moduleService = moduleService;
 	}
 
 	@Override
 	public CharSequence getPageTitle(int position) {
 		Locale l = Locale.getDefault();
 		return String.format(l, "%d. Semester", position + 1);
 	}
 
 }
