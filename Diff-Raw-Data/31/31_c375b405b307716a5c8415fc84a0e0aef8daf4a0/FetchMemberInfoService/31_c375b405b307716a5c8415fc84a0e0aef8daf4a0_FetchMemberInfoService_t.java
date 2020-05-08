 package com.lorent.lvmc.service;
 
import org.apache.log4j.Logger;
 
 import com.lorent.common.dto.LCMRoleDto;
 import com.lorent.lvmc.dto.MemberDto;
 import com.lorent.lvmc.util.DataUtil;
 import com.lorent.lvmc.util.LCMUtil;
 
 public class FetchMemberInfoService {
 
	private Logger log = Logger.getLogger(FetchMemberInfoService.class);
	
 	public MemberDto getMemberDtoByName(String name)throws Exception{
         MemberDto temp = new MemberDto();
         temp.setName(name);
         temp.setOnline(true);
 //        Map<String, LCMRoleDto> confUserRole = LCMUtil.getConfUserRole(DataUtil.getLoginInfo().getConfno(), new String[]{name});
        try{
        	LCMRoleDto roleDto = LCMUtil.getMyRoleAndPermission(DataUtil.getLoginInfo().getConfno(), name);
            temp.setNickname(roleDto.getNickname());
            temp.setRole(roleDto);
        }catch(Exception e){
        	log.error("getMemberDtoByName", e);
        	temp.setNickname(name);
        }
         return temp;
     }
 	
 }
