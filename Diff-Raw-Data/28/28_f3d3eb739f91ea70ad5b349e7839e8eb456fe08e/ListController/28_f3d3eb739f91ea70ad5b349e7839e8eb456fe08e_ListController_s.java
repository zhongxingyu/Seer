 package com.rockontrol.yaogan.controller;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import com.rockontrol.yaogan.model.Place;
 import com.rockontrol.yaogan.model.Shapefile;
 import com.rockontrol.yaogan.model.User;
 import com.rockontrol.yaogan.service.ISecurityManager;
 import com.rockontrol.yaogan.service.IYaoganService;
 import com.rockontrol.yaogan.vo.Page;
 import com.rockontrol.yaogan.vo.ShapefileGroupVo;
 import com.rockontrol.yaogan.vo.ShapefileVo;
 
 @Controller
 @RequestMapping("admin/place")
 public class ListController {
    @Autowired
    private IYaoganService _service;
    @Autowired
    private ISecurityManager _secMgr;
 
    @RequestMapping(value = "", method = { RequestMethod.GET, RequestMethod.POST })
    public String list(Model model,
          @RequestParam(value = "pager.offset", defaultValue = "0") int offset,
         @RequestParam(value = "pageSize", defaultValue = "14") int pageSize) {
       User caller = _secMgr.currentUser();
       int pageNum = offset / pageSize + 1;
       Page<ShapefileGroupVo> page = _service.getShapefilesOfUserByPage(caller, pageNum,
             pageSize);
       model.addAttribute("page", page);
       model.addAttribute("actionUrl", "./place");
       model.addAttribute("places",
             _service.getPlacesVisibleToUser(caller, caller.getId()));
       return "/admin/stats/shapefileList";
    }
 
    @RequestMapping("/fileList")
    public String list(Model model, @RequestParam("placeId") Long placeId,
          @RequestParam("shootTime") String shootTime,
          @RequestParam(value = "pager.offset", defaultValue = "0") int offset,
         @RequestParam(value = "pageSize", defaultValue = "14") int pageSize) {
       if (placeId == null) {
          return "redirect:/admin/place";
       }
       Place place = _service.getPlaceById(placeId);
       User caller = _secMgr.currentUser();
       int pageNum = offset / pageSize + 1;
       Page<ShapefileGroupVo> page = _service.filterShapefiles(caller, place.getId(),
             shootTime, pageNum, pageSize);
       model.addAttribute("page", page);
       model.addAttribute("actionUrl", "./fileList");
 
       model.addAttribute("curPlaceId", placeId);
       model.addAttribute("curShootTime", shootTime);
       model.addAttribute("shootTimes", _service.getAvailableTimeOptions(caller, placeId));
       model.addAttribute("places",
             _service.getPlacesVisibleToUser(caller, caller.getId()));
       model.addAttribute("placeId", placeId);
       model.addAttribute("shootTime", shootTime);
       return "/admin/stats/shapefileList";
    }
 
    public void setSecMgr(ISecurityManager secMgr) {
       this._secMgr = secMgr;
    }
 
    public void setYaoganService(IYaoganService service) {
       this._service = service;
    }
 
    private List<ShapefileGroupVo> groupShapefiles(List<Shapefile> list) {
       Map<String, ShapefileGroupVo> groupVoMap = new HashMap<String, ShapefileGroupVo>();
       for (Shapefile shpfile : list) {
          String placeName = shpfile.getPlace().getName();
          ShapefileGroupVo vo = groupVoMap.get(placeName);
          if (vo == null) {
             vo = new ShapefileGroupVo();
             vo.setPlaceName(placeName);
             groupVoMap.put(placeName, vo);
          }
          ShapefileVo shpvo = toShapefileVo(shpfile);
          vo.addShapefileVo(shpvo);
 
       }
       return new ArrayList(groupVoMap.values());
    }
 
    private ShapefileVo toShapefileVo(Shapefile shpfile) {
       ShapefileVo vo = new ShapefileVo();
       vo.setFileName(shpfile.getFileName());
       vo.setPlace(shpfile.getPlace().getName());
       vo.setShootTime(shpfile.getShootTime());
       vo.setUploadTime(shpfile.getUploadTime());
       vo.setTypeString(shpfile.getTypeString());
       return vo;
    }
 }
