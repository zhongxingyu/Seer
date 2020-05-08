 package com.astrider.sfc.src.helper;
 
 import java.util.ArrayList;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 
 import com.astrider.sfc.app.lib.util.MathUtils;
 import com.astrider.sfc.src.model.dao.NutrientDao;
 import com.astrider.sfc.src.model.dao.RecipeDao;
 import com.astrider.sfc.src.model.dao.WeeklyLogDao;
 import com.astrider.sfc.src.model.vo.db.RecipeVo;
 import com.astrider.sfc.src.model.vo.db.UserVo;
 import com.astrider.sfc.src.model.vo.db.WeeklyLogVo;
 import static com.astrider.sfc.ApplicationContext.*;
 
 /**
  * 汎用ヘルパークラス.
  * 
  * @author astrider
  * 
  */
 public final class SanteUtils {
 	/**
 	 * @author astrider
 	 *         <p>
 	 *         不足している栄養素情報を格納するためのBean
 	 *         </p>
 	 */
 	public static class InsufficientNutrients {
 		private int primaryKey;
 		private int secondaryKey;
 		private String primaryNutrientName;
 		private String secondaryNutrientName;
 
 		public int getPrimaryKey() {
 			return primaryKey;
 		}
 
 		public void setPrimaryKey(int primaryKey) {
 			this.primaryKey = primaryKey;
 		}
 
 		public int getSecondaryKey() {
 			return secondaryKey;
 		}
 
 		public void setSecondKey(int secondKey) {
 			this.secondaryKey = secondKey;
 		}
 
 		public String getPrimaryNutrientName() {
 			return primaryNutrientName;
 		}
 
 		public void setPrimaryNutrientName(String priamryNutrientName) {
 			this.primaryNutrientName = priamryNutrientName;
 		}
 
 		public String getSecondaryNutrientName() {
 			return secondaryNutrientName;
 		}
 
 		public void setSecondaryNutrientName(String secondaryNutrientName) {
 			this.secondaryNutrientName = secondaryNutrientName;
 		}
 	}
 
 	public static UserVo getLoginUser(HttpServletRequest request) {
 		HttpSession session = request.getSession();
 		UserVo user = (UserVo) session.getAttribute(SESSION_USER);
 		return user;
 	}
 
 	/**
 	 * 不足栄養素取得.
 	 * 
 	 * @param userId
 	 * @return InsufficientNutrients
 	 *         <p>
 	 *         最新の栄養素状態から、不足している栄養素2種を割り出す。　 値が全て空の場合は肉と野菜を含むレシピを選択。
 	 *         </p>
 	 */
 	public static InsufficientNutrients getInsufficientNutrients(int userId) {
 		double[] balances = getNutrientBalances(userId, 0);
 		if (balances == null) {
 			return getDefaultNutrients();
 		}
 
 		// 値が空ならば肉と野菜を返す
 		boolean allZero = true;
 		for (double item : balances) {
 			allZero = Double.isNaN(item) && allZero;
 		}
 		if (allZero) {
 			return getDefaultNutrients();
 		}
 
 		// 最も不足している栄養素2種を取得
 		double primary = 1;
 		double secondary = 1;
 		int primaryIndex = 0;
 		int secondaryIndex = 0;
 
 		for (int i = 0; i < balances.length; i++) {
 			if (balances[i] < primary) {
 				primary = balances[i];
 				primaryIndex = i;
 			}
 		}
 		for (int j = 0; j < balances.length; j++) {
 			if (primary < balances[j] && balances[j] < secondary) {
 				secondary = balances[j];
 				secondaryIndex = j;
 			}
 		}
 
 		// 値を取得
 		int primaryKey = primaryIndex + 1;
 		int secondaryKey = secondaryIndex + 1;
 		NutrientDao nutrientDao = new NutrientDao();
 		String primaryNutrientName = nutrientDao.selectById(primaryKey).getLogicalName();
 		String secondaryNutrientName = nutrientDao.selectById(secondaryKey).getLogicalName();
 		nutrientDao.close();
 
 		// 値をセット
 		InsufficientNutrients nutrients = new InsufficientNutrients();
 		nutrients.setPrimaryKey(primaryKey);
 		nutrients.setSecondKey(secondaryKey);
 		nutrients.setPrimaryNutrientName(primaryNutrientName);
 		nutrients.setSecondaryNutrientName(secondaryNutrientName);
 
 		return nutrients;
 	}
 
 	private static InsufficientNutrients getDefaultNutrients() {
 		NutrientDao nutrientDao = new NutrientDao();
 		InsufficientNutrients nutrients = new InsufficientNutrients();
 		nutrients.setPrimaryKey(3);
 		nutrients.setSecondKey(5);
 		nutrients.setPrimaryNutrientName(nutrientDao.selectById(3).getLogicalName());
 		nutrients.setSecondaryNutrientName(nutrientDao.selectById(5).getLogicalName());
 		nutrientDao.close();
 		return nutrients;
 	}
 
 	/**
 	 * @param userId
 	 * @param weekAgo
 	 * @return 栄養素別バランス
 	 *         <p>
 	 *         理想量に対して現在の各摂取栄養素を％で取得
 	 *         </p>
 	 */
 	public static double[] getNutrientBalances(int userId, int weekAgo) {
 		// 現在の栄養摂取量と理想量を取得
 		int[] ingested = getIngestedNutrients(userId, weekAgo);
 		int[] desired = getDesiredNutrients();
 		if (ingested == null || desired == null) {
 			return null;
 		}
 
 		// 理想割合から飛び抜けた値を丸める
 		double ingestedAverage = MathUtils.getAverage(ingested);
 		double desiredAverage = MathUtils.getAverage(desired);
 		double coefficient = ingestedAverage / desiredAverage;
 		for (int i = 0; i < SANTE_NUTRIENT_COUNT; i++) {
 			// 丸め処理
 			int limit = (int) (desired[i] * coefficient);
 			if (limit < ingested[i]) {
 				ingested[i] = limit;
 			}
 		}
 
 		// 栄養バランスを再計算
 		ingestedAverage = MathUtils.getAverage(ingested);
 		desiredAverage = MathUtils.getAverage(desired);
 		double[] items = new double[SANTE_NUTRIENT_COUNT];
 		for (int j = 0; j < SANTE_NUTRIENT_COUNT; j++) {
 			double desiredDiff = (desired[j] - desiredAverage) / desiredAverage;
 			double ingestedDiff = (ingested[j] - ingestedAverage) / ingestedAverage;
 			double diff = (ingestedDiff - desiredDiff) / 2 + 0.5;
 			items[j] = diff;
 		}
 
 		return items;
 	}
 
 	/**
 	 * @param userId
 	 * @param weekAgo
 	 * @return 栄養素別摂取総量
 	 */
 	public static int[] getIngestedNutrients(int userId, int weekAgo) {
 		WeeklyLogDao weekDao = new WeeklyLogDao();
 		WeeklyLogVo weekVo = weekDao.selectByWeekAgo(userId, weekAgo);
 		weekDao.close();
 
 		if (weekVo == null) {
 			return null;
 		}
 
 		int[] ingested = { weekVo.getMilk(), weekVo.getEgg(), weekVo.getMeat(), weekVo.getBean(),
 				weekVo.getVegetable(), weekVo.getFruit(), weekVo.getMineral(), weekVo.getCrop(), weekVo.getPotato(),
 				weekVo.getFat(), weekVo.getSuguar() };
 		return ingested;
 	}
 
 	/**
 	 * @return 栄養素別目標摂取量
 	 */
 	public static int[] getDesiredNutrients() {
 		int[] desired = new int[SANTE_NUTRIENT_COUNT];
 		NutrientDao nutrientDao = new NutrientDao();
		for (int i = 1; i < desired.length; i++) {
 			desired[i] = nutrientDao.selectById(i + 1).getDailyRequiredAmount();
 		}
 		nutrientDao.close();
 		return desired;
 	}
 
 	/**
 	 * @param userId
 	 * @param 取得件数上限
 	 * @return おすすめレシピVO
 	 */
 	public static ArrayList<RecipeVo> getRecommendedRecipes(int userId, int limit) {
 		InsufficientNutrients nutrients = getInsufficientNutrients(userId);
 		ArrayList<RecipeVo> recipes = getRecipesContainBothNutrients(nutrients, limit);
 		return recipes;
 	}
 
 	private static ArrayList<RecipeVo> getRecipesContainBothNutrients(InsufficientNutrients nutrients, int limit) {
 		ArrayList<RecipeVo> recipes = new ArrayList<RecipeVo>();
 		RecipeDao recipeDao = new RecipeDao();
 		ArrayList<Integer> recipesContainsBoth = getDuplicateKeys(
 				recipeDao.selectRecipeIdByNutrientId(nutrients.getPrimaryKey()),
 				recipeDao.selectRecipeIdByNutrientId(nutrients.getSecondaryKey()));
 		if (recipesContainsBoth.size() < limit) {
 			limit = recipesContainsBoth.size();
 		}
 		for (int i = 0; i < limit; i++) {
 			int recipeId = recipesContainsBoth.get(i);
 			RecipeVo recipe = recipeDao.selectByRecipeId(recipeId);
 			recipes.add(recipe);
 		}
 		recipeDao.close();
 
 		return recipes;
 	}
 
 	private static ArrayList<Integer> getDuplicateKeys(ArrayList<Integer> firstKeys, ArrayList<Integer> secondKeys) {
 		ArrayList<Integer> duplicate = new ArrayList<Integer>();
 		for (int first : firstKeys) {
 			for (int second : secondKeys) {
 				if (first == second) {
 					duplicate.add(first);
 				}
 			}
 		}
 		return duplicate;
 	}
 }
