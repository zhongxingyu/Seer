 package com.astrider.sfc.src.model.dao;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import com.astrider.sfc.app.lib.BaseDao;
 import com.astrider.sfc.app.lib.Mapper;
 import com.astrider.sfc.src.model.vo.db.MaterialQuantityVo;
 import com.astrider.sfc.src.model.vo.db.RecipeVo;
 import com.astrider.sfc.src.model.vo.db.StepVo;
 
 public class RecipeDao extends BaseDao {
 	public RecipeDao() {
 		super();
 	}
 
 	public RecipeDao(Connection con) {
 		super(con);
 	}
 
 	public ArrayList<RecipeVo> selectAll() {
 		ArrayList<RecipeVo> recipes = new ArrayList<RecipeVo>();
 		PreparedStatement pstmt = null;
 
 		try {
 			String sql = "SELECT * FROM recipes";
 			pstmt = con.prepareStatement(sql);
 			ResultSet rs = pstmt.executeQuery();
 			while (rs.next()) {
 				Mapper<RecipeVo> mapper = new Mapper<RecipeVo>();
 				recipes.add(mapper.fromResultSet(rs));
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				if (pstmt != null) {
 					pstmt.close();
 				}
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 
 		return recipes;
 	}
 
 	public RecipeVo selectByRecipeId(int recipeId) {
 		RecipeVo recipe = null;
 		PreparedStatement pstmt = null;
 
 		try {
 			String sql = "SELECT * FROM recipes WHERE recipe_id = ?";
 			pstmt = con.prepareStatement(sql);
 			pstmt.setInt(1, recipeId);
 			ResultSet rs = pstmt.executeQuery();
 			while (rs.next()) {
 				Mapper<RecipeVo> mapper = new Mapper<RecipeVo>();
 				recipe = mapper.fromResultSet(rs);
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				if (pstmt != null) {
 					pstmt.close();
 				}
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 
 		return recipe;
 	}
 
 	public ArrayList<RecipeVo> selectByMaterialName(String materialName) {
 		ArrayList<RecipeVo> recipes = new ArrayList<RecipeVo>();
 		PreparedStatement pstmt = null;
 
 		try {
			StringBuilder sb = new StringBuilder();
			sb.append("SELECT * FROM recipes WHERE recipe_id IN ");
			sb.append("(SELECT DISTINCT recipe_id FROM RECIPE_MATERIALS WHERE material_name LIKE ?) ");
			sb.append("AND rownum between 1 and 20");
			pstmt = con.prepareStatement(sb.toString());
 			pstmt.setString(1, "%" + materialName + "%");
 			ResultSet rs = pstmt.executeQuery();
 			while (rs.next()) {
 				Mapper<RecipeVo> mapper = new Mapper<RecipeVo>();
 				RecipeVo recipe = mapper.fromResultSet(rs);
 				recipes.add(recipe);
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				if (pstmt != null) {
 					pstmt.close();
 				}
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 
 		return recipes;
 	}
 
 	public ArrayList<RecipeVo> selectByNutrientId(int nutrientId) {
 		ArrayList<RecipeVo> recipes = new ArrayList<RecipeVo>();
 		PreparedStatement pstmt = null;
 
 		try {
			StringBuilder sb = new StringBuilder();
			sb.append("SELECT * FROM recipes WHERE recipe_id IN ");
			sb.append("(SELECT DISTINCT recipe_id FROM RECIPE_MATERIAL_QUANTITIES WHERE nutrient_id = ?) ");
			sb.append("AND rownum between 1 and 20");
			pstmt = con.prepareStatement(sb.toString());
 			pstmt.setInt(1, nutrientId);
 			ResultSet rs = pstmt.executeQuery();
 			Mapper<RecipeVo> mapper = new Mapper<RecipeVo>();
 			while (rs.next()) {
 				recipes.add(mapper.fromResultSet(rs));
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				if (pstmt != null) {
 					pstmt.close();
 				}
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 		return recipes;
 	}
 
 	public ArrayList<MaterialQuantityVo> selectMaterialQuantitiesByRecipeId(int recipeId) {
 		ArrayList<MaterialQuantityVo> materialQuantities = new ArrayList<MaterialQuantityVo>();
 		PreparedStatement pstmt = null;
 
 		try {
 			String sql = "SELECT * FROM RECIPE_MATERIAL_QUANTITIES WHERE recipe_id = ? ORDER BY nutrient_id";
 			pstmt = con.prepareStatement(sql);
 			pstmt.setInt(1, recipeId);
 			ResultSet rs = pstmt.executeQuery();
 			Mapper<MaterialQuantityVo> mapper = new Mapper<MaterialQuantityVo>();
 			while (rs.next()) {
 				MaterialQuantityVo materialQuantity = mapper.fromResultSet(rs);
 				materialQuantities.add(materialQuantity);
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				if (pstmt != null) {
 					pstmt.close();
 				}
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 		return materialQuantities;
 	}
 
 	public ArrayList<MaterialQuantityVo> selectMaterialQuantitiesByNutrientId(int nutrientId) {
 		ArrayList<MaterialQuantityVo> materials = new ArrayList<MaterialQuantityVo>();
 		PreparedStatement pstmt = null;
 
 		try {
			String sql = "SELECT * FROM RECIPE_MATERIAL_QUANTITIES WHERE nutrient_id = ? ORDER BY quantity";
 			pstmt = con.prepareStatement(sql);
 			pstmt.setInt(1, nutrientId);
 			ResultSet rs = pstmt.executeQuery();
 			Mapper<MaterialQuantityVo> mapper = new Mapper<MaterialQuantityVo>();
 			while (rs.next()) {
 				MaterialQuantityVo material = mapper.fromResultSet(rs);
 				materials.add(material);
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				if (pstmt != null) {
 					pstmt.close();
 				}
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 		return materials;
 	}
 
 	public ArrayList<StepVo> selectStepsByRecipeId(int recipeId) {
 		ArrayList<StepVo> steps = new ArrayList<StepVo>();
 		PreparedStatement pstmt = null;
 
 		try {
			String sql = "SELECT * FROM steps WHERE recipe_id = ? ORDER BY step";
 			pstmt = con.prepareStatement(sql);
 			pstmt.setInt(1, recipeId);
 			ResultSet rs = pstmt.executeQuery();
 			Mapper<StepVo> mapper = new Mapper<StepVo>();
 			while (rs.next()) {
 				StepVo step = mapper.fromResultSet(rs);
 				steps.add(step);
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				if (pstmt != null) {
 					pstmt.close();
 				}
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 		return steps;
 	}
 
 	public ArrayList<Integer> selectRecipeIdByNutrientId(int nutrientId) {
 		ArrayList<Integer> recipeIds = new ArrayList<Integer>();
 		PreparedStatement pstmt = null;
 
 		try {
 			String sql = "SELECT DISTINCT recipe_id FROM RECIPE_MATERIAL_QUANTITIES WHERE nutrient_id = ?";
 			pstmt = con.prepareStatement(sql);
 			pstmt.setInt(1, nutrientId);
 			ResultSet rs = pstmt.executeQuery();
 			while (rs.next()) {
 				recipeIds.add(rs.getInt(1));
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				if (pstmt != null) {
 					pstmt.close();
 				}
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 		return recipeIds;
 	}
 	
 	public HashMap<Integer, ArrayList<MaterialQuantityVo>> selectMaterialListByRecipeList(ArrayList<RecipeVo> recipes) {
 		HashMap<Integer, ArrayList<MaterialQuantityVo>> materialList = new HashMap<Integer, ArrayList<MaterialQuantityVo>>();
 		PreparedStatement pstmt = null;
 		
 		try {
 			StringBuilder sb = new StringBuilder();
 			sb.append("SELECT * FROM recipe_material_quantities WHERE recipe_id IN (");
 			for (int i = 0; i < recipes.size();) {
 				sb.append("?");
 				if (++i < recipes.size()) {
 					sb.append(",");
 				}
 			}
 			sb.append(")");
			sb.append(" ORDER BY nutrient_id");
 			
 			pstmt = con.prepareStatement(sb.toString());
 			
 			for (int j = 0; j < recipes.size(); j++) {
 				pstmt.setObject(j + 1, recipes.get(j).getRecipeId());
 			}
 			
 			ResultSet rs = pstmt.executeQuery();
 			while (rs.next()) {
 				Mapper<MaterialQuantityVo> mapper = new Mapper<MaterialQuantityVo>();
 				MaterialQuantityVo material = mapper.fromResultSet(rs);
 				
 				ArrayList<MaterialQuantityVo> materials = materialList.get(material.getRecipeId());
 				if (materials == null) {
 					materials = new ArrayList<MaterialQuantityVo>();
 				}
 				materials.add(material);
 				materialList.put(material.getRecipeId(), materials);
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				if (pstmt != null) {
 					pstmt.close();
 				}
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 		
 		return materialList;
 	}
 }
