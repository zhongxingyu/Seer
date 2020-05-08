 /*
  * Copyright (c) 2009. Orange Leap Inc. Active Constituent
  * Relationship Management Platform.
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.orangeleap.tangerine.service;
 
 import com.orangeleap.tangerine.domain.PostBatch;
 import com.orangeleap.tangerine.domain.Segmentation;
 import com.orangeleap.tangerine.web.common.SortInfo;
 
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 
 public interface PostBatchService {
 
 //    public Map<String, String> readAllowedGiftSelectFields();
 //    public Map<String, String> readAllowedGiftUpdateFields();
 //    public List<PostBatch> listBatchs();
 //    public PostBatch readBatch(Long batchId);
     public PostBatch maintainBatch(PostBatch postbatch);

 	public PostBatch updateBatch(PostBatch postbatch);

 	public void deleteBatch(PostBatch postbatch);

 //    public List<AbstractPaymentInfoEntity> createBatchSelectionList(PostBatch postbatch);
 //    public List<AbstractPaymentInfoEntity> getBatchSelectionList(PostBatch postbatch);
 //	public PaginatedResult getBatchSelectionList(long postbatchId, SortInfo sortInfo);
 
     List<PostBatch> readBatches(boolean batchUpdated, SortInfo sort, Locale locale);
 
    PostBatch readBatch(Long batchId);

     PostBatch readBatchCreateIfNull(final Long batchId);
 
     List<Map<String, Object>> findSegmentationsForBatchType(PostBatch batch, String batchType, String sortField, String sortDirection, int startIndex, int resultCount);
     
     List<Segmentation> findSegmentations(String batchType, String sortField, String sortDirection, int startIndex, int resultCount);
 
     int findTotalSegmentations(String batchType);
 }
