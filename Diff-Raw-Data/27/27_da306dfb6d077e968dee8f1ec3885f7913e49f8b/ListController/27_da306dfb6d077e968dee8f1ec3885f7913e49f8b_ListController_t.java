 package com.flatmates.board.controller;
 
 import com.flatmates.board.domain.entity.BuildingComplex;
 import com.flatmates.board.domain.entity.BulletinBoard;
 import com.flatmates.board.domain.entity.Comment;
 import com.flatmates.board.domain.entity.Sticker;
 import com.flatmates.board.domain.service.BuildingComplexService;
 import com.flatmates.board.domain.service.BulletinBoardService;
 import com.flatmates.board.domain.service.CommentService;
 import java.util.ArrayList;
 import java.util.Collection;
import java.util.HashMap;
 import java.util.List;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;

 /**
  *
  * @author kavan soleimanbeigi
  */
 
 @Controller
 @RequestMapping(value = "/list")
public class ListController {
 
     @Autowired
     private BulletinBoardService boardService;
     @Autowired
     private BuildingComplexService buildingService;
     @Autowired
     private CommentService commentService;
     @Autowired
     private CommentService stickerTypeService;
    
     @RequestMapping(method = RequestMethod.GET)
     public @ResponseBody
     String listRoot() {
         return "what do you want to list?";
     }
 
    @RequestMapping(value = "/address/{building_id}", method = RequestMethod.GET)
    public @ResponseBody
    String getBuildingAddress(@PathVariable String building_id) {
        String building_address = buildingService.findBuildingById(building_id).getAddress();

        return building_address;
    }

     @RequestMapping(value = "/buildings", method = RequestMethod.GET)
     public @ResponseBody
     Collection<BuildingComplex> listBuildingComplex() {
         Collection<BuildingComplex> res = buildingService.listAll();
         return res;
     }

     @RequestMapping(value = "/stickers/{building_id}", method = RequestMethod.GET)
     public @ResponseBody
     List<Sticker> listBuildingStickers(@PathVariable String building_id) {
         Collection<BulletinBoard> boards = boardService.listAllBoards();
         String boardId = ControlTool.findBoardIdByBuilding(boards, building_id);
         Collection<Sticker> actual_result = boardService.findAllStickers(boardId);
         List<Sticker> secured_list = ControlTool.clearAuthenticationDataFromStickers(actual_result);

         return secured_list;
     }

     @RequestMapping(value = "/comments/sticker_id", method = RequestMethod.GET)
     public @ResponseBody
     Collection<Comment> listStickerComments(@PathVariable String sticker_id) {
         return commentService.findByStickerId(sticker_id);
     }

     @RequestMapping(value = "/types", method = RequestMethod.GET)
     public @ResponseBody
     Collection<Comment> listStickerTypes() {
         return stickerTypeService.listAll();
     }
 }
