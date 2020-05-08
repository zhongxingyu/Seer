 /*
  * BannerController.java
  */
 
 package com.topfirst.web.page_controllers;
 
 import java.util.Collection;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.concurrent.atomic.AtomicReference;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.SessionScoped;
 
 import com.topfirst.backend.BackEnd;
 import com.topfirst.backend.BannerManager;
 import com.topfirst.backend.entities.Banner;
 import com.topfirst.backend.entities.User;
 import com.topfirst.backend.exceptions.PersistenceException;
 import com.topfirst.web.entity_wrappers.BannerExt;
 
 /**
  * Description.
  *
  * @author Rod Odin
  */
 @ManagedBean(name = "bannerController")
 @SessionScoped
 public class BannerController
 	extends AbstractController
 {
 // Constants -----------------------------------------------------------------------------------------------------------
 
 	public static final String DEFAULT_RESOURCE_BUNDLE_NAME = BannerController.class.getName();
 
 	public static final long LAST_DAY_PERIOD   =  24 * 60 * 60 * 1000L;
 	public static final long LAST_WEEK_PERIOD  =   7 * LAST_DAY_PERIOD;
 	public static final long LAST_MONTH_PERIOD =  30 * LAST_DAY_PERIOD;
 	public static final long LAST_YEAR_PERIOD  = 365 * LAST_DAY_PERIOD;
 
 	public static final int TOP_ALL_BANNERS_COUNT   = 1000;
 	public static final int TOP_USERS_BANNERS_COUNT = 1000;
 	public static final int TOP_DAY_BANNERS_COUNT   =   25;
 	public static final int TOP_WEEK_BANNERS_COUNT  =  100;
 	public static final int TOP_MONTH_BANNERS_COUNT =  250;
 	public static final int TOP_YEAR_BANNERS_COUNT  =  500;
 	public static final int ALL_TODAY_BANNERS_COUNT =    0; // 0 means there is no limit
 
 // Constructors --------------------------------------------------------------------------------------------------------
 
 	public BannerController()
 	{
 		super(DEFAULT_RESOURCE_BUNDLE_NAME);
 	}
 
 // Getters/Setters -----------------------------------------------------------------------------------------------------
 
 	public List<BannerExt> getTopBanners()
 	{
 		final List<BannerExt> rv = new LinkedList<>();
 		try
 		{
 			//final UserManager userManager = BackEnd.getDefaultBackend().getUserManager();
 			//final Map<String,User> testUsers = userManager.checkAndPopulateTestUsers(userManager.getTestUserEmails());
 
 			final BannerManager bannerManager = BackEnd.getDefaultBackend().getBannerManager();
 			//bannerManager.checkAndPopulateTestBanners(testUsers);
 
 			final Collection<? extends Banner> allBanners = bannerManager.getAllBanners(BannerManager.BannerSortMode.ByRank, TOP_ALL_BANNERS_COUNT);
 			for (Banner banner : allBanners)
				rv.add(new BannerExt(bannerManager, banner));
 		}
 		catch (PersistenceException x)
 		{
 			LOG.error("Unable to get Top Banners", x);
 		}
 		return rv;
 	}
 
 	public List<BannerExt> getAllTodayBanners()
 	{
 		final List<BannerExt> rv = new LinkedList<>();
 		try
 		{
 			final BannerManager bannerManager = BackEnd.getDefaultBackend().getBannerManager();
 			final Collection<? extends Banner> allBanners = bannerManager.getAllBanners(BannerManager.BannerSortMode.ByDate, ALL_TODAY_BANNERS_COUNT);
 			for (Banner banner : allBanners)
				rv.add(new BannerExt(bannerManager, banner));
 		}
 		catch (PersistenceException x)
 		{
 			LOG.error("Unable to get Today Banners", x);
 		}
 		return rv;
 	}
 
 	public List<BannerExt> getTopDayBanners()
 	{
 		final Date endTime = new Date();
 		final Date startTime = new Date(endTime.getTime() - LAST_DAY_PERIOD);
 		return doGetTopPeriodBanners(startTime, endTime, TOP_DAY_BANNERS_COUNT);
 	}
 
 	public List<BannerExt> getTopWeekBanners()
 	{
 		final Date endTime = new Date();
 		final Date startTime = new Date(endTime.getTime() - LAST_WEEK_PERIOD);
 		return doGetTopPeriodBanners(startTime, endTime, TOP_WEEK_BANNERS_COUNT);
 	}
 
 	public List<BannerExt> getTopMonthBanners()
 	{
 		final Date endTime = new Date();
 		final Date startTime = new Date(endTime.getTime() - LAST_MONTH_PERIOD);
 		return doGetTopPeriodBanners(startTime, endTime, TOP_MONTH_BANNERS_COUNT);
 	}
 
 	public List<BannerExt> getTopYearBanners()
 	{
 		final Date endTime = new Date();
 		final Date startTime = new Date(endTime.getTime() - LAST_YEAR_PERIOD);
 		return doGetTopPeriodBanners(startTime, endTime, TOP_YEAR_BANNERS_COUNT);
 	}
 
 	public List<BannerExt> getUserBanners()
 	{
 		final List<BannerExt> rv = new LinkedList<>();
 		try
 		{
 
 			final BannerManager bannerManager = BackEnd.getDefaultBackend().getBannerManager();
 			User user=selectedBanner.get().getUser();
 //			LOG.info("selectedBanner.user.id="+user.getId());
 
 			final Collection<? extends Banner> banners = bannerManager.getBannersOfUser(user, BannerManager.BannerSortMode.ByRank, TOP_USERS_BANNERS_COUNT);
 			for (Banner banner : banners)
				rv.add(new BannerExt(bannerManager, banner));
			}
 		catch (PersistenceException x)
 		{
 			LOG.error("Unable to get User Banners", x);
 		}
 		return rv;
 	}
 
 	public BannerExt getSelectedBanner()
 	{
 		//LOG.info("Banner get: selectedBanner=" + selectedBanner.get().getTitle());
 		return selectedBanner.get();
 	}
 
 	public void setSelectedBanner(BannerExt selectedBanner)
 	{
 		//LOG.info("Banner set: selectedBanner=" + (selectedBanner != null ? (selectedBanner.getTitle()) : null));
 		this.selectedBanner.set(selectedBanner);
 	}
 
 // Internal Logic ------------------------------------------------------------------------------------------------------
 
 	public List<BannerExt> doGetTopPeriodBanners(Date startTime, Date endTime, int howMany)
 	{
 		final List<BannerExt> rv = new LinkedList<>();
 		try
 		{
 			final BannerManager bannerManager = BackEnd.getDefaultBackend().getBannerManager();
 			final Collection<? extends Banner> banners = bannerManager.getBannersForPeriod(BannerManager.BannerSortMode.ByRank, startTime, endTime, howMany);
 			for (Banner banner : banners)
				rv.add(new BannerExt(bannerManager, banner));
 		}
 		catch (PersistenceException x)
 		{
 			LOG.error("Unable to get Top Period Banners", x);
 		}
 		return rv;
 	}
 
 
 // Attributes ----------------------------------------------------------------------------------------------------------
 
 	//private final List<Banner> banners = new LinkedList<>();
 	private final AtomicReference<BannerExt> selectedBanner = new AtomicReference<>();
 }
