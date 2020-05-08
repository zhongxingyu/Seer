 /*
  * Copyright (C) 2013 Alexandre Thomazo
  *
  * This file is part of BankIt.
  *
  * BankIt is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * BankIt is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with BankIt. If not, see <http://www.gnu.org/licenses/>.
  */
 package org.alexlg.bankit.dao;
 
 import java.math.BigDecimal;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.persistence.Tuple;
 import javax.persistence.criteria.CriteriaBuilder;
 import javax.persistence.criteria.CriteriaQuery;
 import javax.persistence.criteria.Expression;
 import javax.persistence.criteria.Join;
 import javax.persistence.criteria.Root;
 
 import org.alexlg.bankit.db.Category;
 import org.alexlg.bankit.db.Category_;
 import org.alexlg.bankit.db.Operation;
 import org.alexlg.bankit.db.Operation_;
 import org.joda.time.LocalDate;
 import org.joda.time.YearMonth;
 import org.springframework.stereotype.Controller;
 
 /**
  * DAO for categories
  * 
  * @author Alexandre Thomazo
  */
 @Controller
 public class CategoryDao extends AbstractDao<Category, Integer> {
 
 	/**
 	 * Calculate the amount of operations for all categories on a specific month
 	 * @param yearMonth Year and month of the summary to calculate
 	 * @return Map containing the Category and the amount for the month
 	 */
 	public Map<Category, BigDecimal> getMonthSummary(YearMonth yearMonth) {
 		CriteriaBuilder b = getBuilder();
 		
 		//SELECT PASSED OPERATION
 		//create criteria and join
 		CriteriaQuery<Tuple> q = b.createTupleQuery();
 		Root<Operation> operation = q.from(Operation.class);
 		Join<Operation, Category> category = operation.join(Operation_.category);
 		
 		//select
 		//sum all amount operation for operation imported from the bank
 		Expression<BigDecimal> sum = b.sum(operation.get(Operation_.amount));
 
 		//sum only planned amount if the amount is not set (as we have a planned operation)
 		//we use a sum(case when xx end) for that
 		//in sql, it will be translated into : sum(case when o.amount is null then o.planned otherwise 0 end)
 		Expression<BigDecimal> sumPlanned =
 				b.sum(b.<BigDecimal>selectCase()
 						.when(b.isNull(operation.get(Operation_.amount)), operation.get(Operation_.planned))
 						.otherwise(BigDecimal.ZERO));
 
 		//select the 3 fields into a tuple
 		q.select(b.tuple(category, sum, sumPlanned));
 		
 		//where clause
 		LocalDate startDate = yearMonth.toLocalDate(1);
 		LocalDate endDate = startDate.withDayOfMonth(startDate.dayOfMonth().getMaximumValue());
 		q.where(b.between(operation.get(Operation_.operationDate), startDate.toDate(), endDate.toDate()));
 
 		//group by
 		q.groupBy(category.get(Category_.categoryId));
 
 		//order by
 		q.orderBy(b.asc(category.get(Category_.name)));
 
 		//execute query
 		List<Tuple> results = getEm().createQuery(q).getResultList();
 		
 		//put in map
 		Map<Category, BigDecimal> resMap = new LinkedHashMap<Category, BigDecimal>(results.size());
 		for (Tuple res : results) {
 			BigDecimal sumVal = res.get(sum);
 			BigDecimal sumPlannedVal = res.get(sumPlanned);
			if (sumVal == null) sumVal = BigDecimal.ZERO;
			if (sumPlannedVal == null) sumPlannedVal = BigDecimal.ZERO;

 			if (!sumVal.equals(BigDecimal.ZERO) || !sumPlannedVal.equals(BigDecimal.ZERO)) {
 				resMap.put(res.get(category), sumVal.add(sumPlannedVal));
 			}
 		}
 				
 		return resMap;
 	}
 
 	@Override
 	public List<Category> getList() {
 		CriteriaBuilder b = getBuilder();
 		
 		//creating criteria
 		CriteriaQuery<Category> q = b.createQuery(Category.class);
 		Root<Category> category = q.from(Category.class);
 		q.select(category);
 		
 		//ordering
 		q.orderBy(b.asc(category.get(Category_.name)));
 		
 		return getEm().createQuery(q).getResultList();
 	}
 	
 }
