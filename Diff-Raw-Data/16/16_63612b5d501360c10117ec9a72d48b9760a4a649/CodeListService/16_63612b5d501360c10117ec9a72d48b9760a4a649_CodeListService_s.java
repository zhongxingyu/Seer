 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements. See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership. The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied. See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 package com.evidence.service;
 
 import java.util.List;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.evidence.dao.StateDAO;
 import com.evidence.dto.StateDTO;
 import com.evidence.entity.State;
 import com.evidence.utility.DTOConverter;
 
 /**
  * @author Michal Bocek
  * @since 1.0.0
  */
 @Service
 @Transactional(readOnly = true)
 public class CodeListService {
 
 	@Autowired
 	private StateDAO stateDAO;
 
 	public List<StateDTO> getStates() {
 		List<State> states = stateDAO.findAll();
 		List<StateDTO> stateDTOs = DTOConverter.convertList(states, StateDTO.class);
 		return stateDTOs;
 	}
 }
