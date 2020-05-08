 /**
  * Copyright (C) 2011  JTalks.org Team
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
  */
 package org.jtalks.poulpe.web.controller.section.mvvm;
 
 import org.jtalks.common.model.entity.ComponentType;
 import org.jtalks.poulpe.model.entity.Jcommune;
 import org.jtalks.poulpe.model.entity.PoulpeBranch;
 import org.jtalks.poulpe.model.entity.PoulpeSection;
 import org.jtalks.poulpe.service.ComponentService;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.DataProvider;
 import org.testng.annotations.Test;
 import org.zkoss.zul.DefaultTreeNode;
 import org.zkoss.zul.TreeModel;
 import org.zkoss.zul.TreeNode;
 
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 import static org.testng.Assert.*;
 
 /**
  * @author stanislav bashkirtsev
  */
 @Test
 public class ForumStructureVmTest {
     private ComponentService componentService;
     private ForumStructureVm vm;
 
     @BeforeMethod
     public void setUp() throws Exception {
         componentService = mock(ComponentService.class);
         vm = new ForumStructureVm(componentService);
     }
 
     @Test(dataProvider = "provideRandomJcommuneWithSections", enabled = false)
     public void testSave(Jcommune jcommune) throws Exception {
         PoulpeSection selectedSection = new PoulpeSection("section", "description");
         when(componentService.getByType(ComponentType.FORUM)).thenReturn(jcommune);
 //        vm.initForumStructure();
         vm.setSelectedNode(new DefaultTreeNode<PoulpeSection>(selectedSection));
         vm.saveSection();
 
         verify(componentService).saveComponent(jcommune);
         assertNull(vm.getSelectedItem().getItem());
         jcommune.getSections().contains(selectedSection);
     }
 
     @Test(enabled = false)
     public void testShowNewSectionDialog_creatingNewSection() throws Exception {
         vm.showNewSectionDialog(true);
         assertNull(vm.getSelectedItem().getItem(PoulpeSection.class).getName(), null);
         assertEquals(vm.getSelectedItem().getItem(PoulpeSection.class).getId(), 0);
         assertTrue(vm.isShowCreateSectionDialogAndSetFalse());
     }
 
     @Test(enabled = false)
     public void testShowNewSectionDialog_creatingNewSectionAfterEditingCanceled() throws Exception {
         vm.setSelectedNode(new DefaultTreeNode<PoulpeSection>(new PoulpeSection("some name", "some description")));
         vm.showNewSectionDialog(true);
         assertNull(vm.getSelectedItem().getItem(PoulpeSection.class).getName(), null);
         assertEquals(vm.getSelectedItem().getItem().getId(), 0);
         assertTrue(vm.isShowCreateSectionDialogAndSetFalse());
     }
 
     @Test(dataProvider = "provideRandomJcommuneWithSections", enabled = false)
     public void testGetSections(Jcommune jcommune) throws Exception {
         when(componentService.getByType(ComponentType.FORUM)).thenReturn(jcommune);
 //        vm.initForumStructure();
        TreeModel treeModel = vm.getSectionTree();
 
         TreeNode root = (TreeNode) treeModel.getRoot();
         assertEquals(root.getChildCount(), jcommune.getSections().size());
         assertEquals(root.getChildAt(1).getChildCount(), jcommune.getSections().get(1).getBranches().size());
         Object branch1OfSection0 = root.getChildAt(0).getChildAt(1).getData();
         assertSame(branch1OfSection0, jcommune.getSections().get(0).getBranches().get(1));
     }
 
     @DataProvider
     public Object[][] provideRandomJcommuneWithSections() {
         Jcommune jcommune = new Jcommune();
         PoulpeSection sectionA = new PoulpeSection("SectionA");
         sectionA.addOrUpdateBranch(new PoulpeBranch("BranchA"));
         sectionA.addOrUpdateBranch(new PoulpeBranch("BranchB"));
         jcommune.addSection(sectionA);
         PoulpeSection sectionB = new PoulpeSection("SectionB");
         sectionB.addOrUpdateBranch(new PoulpeBranch("BranchD"));
         sectionB.addOrUpdateBranch(new PoulpeBranch("BranchE"));
         jcommune.addSection(sectionB);
         return new Object[][]{{jcommune}};
     }
 }
