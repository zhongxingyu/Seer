 /*
  * Sweeper
  * Copyright (C) 2012 Bogdan Pistol
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License version 3
  * as published by the Free Software Foundation.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package gg.pistol.sweeper.core;
 
 import static org.junit.Assert.*;
 import static org.mockito.Mockito.*;
 import static gg.pistol.sweeper.test.ObjectVerifier.*;
 
 import gg.pistol.sweeper.core.SweeperException;
 import gg.pistol.sweeper.core.SweeperOperationListener;
 import gg.pistol.sweeper.core.SweeperTargetAction;
 import gg.pistol.sweeper.core.SweeperTargetImpl;
 import gg.pistol.sweeper.core.SweeperTarget.Mark;
 import gg.pistol.sweeper.core.SweeperTarget.Type;
 import gg.pistol.sweeper.core.resource.Resource;
 import gg.pistol.sweeper.core.resource.ResourceDirectory;
 import gg.pistol.sweeper.core.resource.ResourceFile;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.util.Collections;
 import java.util.Iterator;
 
 import org.joda.time.DateTime;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableSet;
 
 public class SweeperTargetImplTest {
 
     private ResourceFile resource1;
     private ResourceFile resource1Copy;
     private ResourceFile resource2;
     private ResourceDirectory resourceDir;
 
     private SweeperTargetImpl mockedParent;
 
     private SweeperTargetImpl target1;
     private SweeperTargetImpl target1Copy;
     private SweeperTargetImpl target2;
     private SweeperTargetImpl targetDir;
 
     private SweeperOperationListener listener;
 
     @Before
     public void setUp() throws Exception {
         resource1 = mockResourceFile("bar");
         resource1Copy = mockResourceFile("bar");
         resource2 = mockResourceFile("foo");
         resourceDir = mockResourceDirectory("baz", resource1, resource2);
 
         listener = mock(SweeperOperationListener.class);
         mockedParent = mock(SweeperTargetImpl.class);
 
         target1 = new SweeperTargetImpl(resource1, mockedParent);
         target1Copy = new SweeperTargetImpl(resource1Copy, mockedParent);
         target2 = new SweeperTargetImpl(resource2, mockedParent);
         targetDir = new SweeperTargetImpl(resourceDir, mockedParent);
     }
 
     private ResourceFile mockResourceFile(String name) throws Exception {
         ResourceFile res = mock(ResourceFile.class);
         when(res.getName()).thenReturn(name);
         return res;
     }
 
     private ResourceDirectory mockResourceDirectory(String name, Resource... subresources) {
         ResourceDirectory res = mock(ResourceDirectory.class);
         when(res.getName()).thenReturn(name);
         ResourceDirectory.ResourceCollectionResponse subresourceCollection = mock(ResourceDirectory.ResourceCollectionResponse.class);
         when(res.getSubresources()).thenReturn(subresourceCollection);
         when(subresourceCollection.getResources()).thenReturn(ImmutableList.copyOf(subresources));
         return res;
     }
 
     @Test
     public void testRootConstructor() throws Exception {
         SweeperTargetImpl root = new SweeperTargetImpl(ImmutableSet.of((Resource) resource1, (Resource) resource2));
 
         assertEquals("", root.getName());
         assertEquals(Type.ROOT, root.getType());
         assertNull(root.getResource());
         assertNull(root.getParent());
         assertTrue(root.isPartiallyExpanded());
         assertTrue(root.isExpanded());
 
         Iterator<SweeperTargetImpl> children = root.getChildren().iterator();
         assertEquals(resource1, children.next().getResource());
         assertEquals(resource2, children.next().getResource());
 
         try {
             new SweeperTargetImpl(null);
             fail();
         } catch (NullPointerException e) {
             // expected
         }
 
         try {
             new SweeperTargetImpl(Collections.<Resource>emptySet());
             fail();
         } catch (IllegalArgumentException e) {
             // expected
         }
 
     }
 
     @Test
     public void testConstructor() throws Exception {
         assertEquals("bar", target1.getName());
         assertEquals(resource1, target1.getResource());
         assertEquals(mockedParent, target1.getParent());
 
         assertEquals(Type.FILE, target1.getType());
         assertTrue(target1.isPartiallyExpanded());
         assertTrue(target1.isExpanded());
         assertTrue(target1.getChildren().isEmpty());
 
         assertEquals(Type.DIRECTORY, targetDir.getType());
         assertTrue(targetDir.getChildren().isEmpty());
 
         try {
             new SweeperTargetImpl(null, mockedParent);
             fail();
         } catch (NullPointerException e) {
             // expected
         }
 
         try {
             new SweeperTargetImpl(resource1, null);
             fail();
         } catch (NullPointerException e) {
             // expected
         }
 
         try {
             new SweeperTargetImpl(mock(Resource.class), mockedParent);
             fail();
         } catch (IllegalArgumentException e) {
             // expected, resource is not a ResourceFile or a ResourceDirectory
         }
     }
 
     @Test
     public void testExpand() throws Exception {
         for (int i = 1; i <= 2; i++) {
             targetDir.expand(listener);
 
             assertTrue(targetDir.isPartiallyExpanded());
             assertTrue(targetDir.isExpanded());
             Iterator<SweeperTargetImpl> children = targetDir.getChildren().iterator();
             assertEquals(resource1, children.next().getResource());
             assertEquals(resource2, children.next().getResource());
         }
         verify(listener).updateTargetAction(targetDir, SweeperTargetAction.EXPAND);
     }
 
     @Test
     public void testExpandException() throws Exception {
         try {
             targetDir.expand(null);
             fail();
         } catch (NullPointerException e) {
             // expected
         }
 
         when(resourceDir.getSubresources().getExceptions()).thenReturn(ImmutableList.of(new Exception()));
         targetDir.expand(listener);
 
         assertTrue(targetDir.isPartiallyExpanded());
         assertFalse(targetDir.isExpanded());
         verify(listener).updateTargetAction(targetDir, SweeperTargetAction.EXPAND);
         verify(listener).updateTargetException(eq(targetDir), eq(SweeperTargetAction.EXPAND), any(SweeperException.class));
     }
 
     @Test
     public void testComputeSizeFile() throws Exception {
         when(resource1.getSize()).thenReturn(5L);
 
         assertFalse(target1.isPartiallySized());
         assertFalse(target1.isSized());
 
         for (int i = 1; i <= 2; i++) {
             target1.computeSize(listener);
 
             assertTrue(target1.isPartiallyExpanded());
             assertTrue(target1.isSized());
             assertEquals(5L, target1.getSize());
             assertEquals(1, target1.getTotalTargets());
             assertEquals(1, target1.getTotalFiles());
         }
         verify(listener).updateTargetAction(target1, SweeperTargetAction.COMPUTE_SIZE);
     }
 
     private SweeperTargetImpl prepareChildToSize(SweeperTargetImpl target, long size, int totalTargets, int totalFiles) {
         target = spy(target);
         when(target.isPartiallySized()).thenReturn(true);
         when(target.getSize()).thenReturn(size);
         when(target.getTotalTargets()).thenReturn(totalTargets);
         when(target.getTotalFiles()).thenReturn(totalFiles);
         return target;
     }
 
     private SweeperTargetImpl prepareDirToSize(SweeperTargetImpl target, boolean isFullExpanded, SweeperTargetImpl... children) {
         target = spy(target);
         when(target.isPartiallyExpanded()).thenReturn(true);
         when(target.isExpanded()).thenReturn(isFullExpanded);
         when(target.getChildren()).thenReturn(ImmutableList.copyOf(children));
         return target;
     }
 
     private void computeSizeDirectory(boolean isFullExpanded) throws Exception {
         SweeperTargetImpl target1Spy = prepareChildToSize(target1, 5L, 1, 1);
         SweeperTargetImpl target2Spy = prepareChildToSize(target2, 6L, 3, 2);
         SweeperTargetImpl targetDirSpy = prepareDirToSize(targetDir, isFullExpanded, target1Spy, target2Spy);
 
         assertFalse(targetDirSpy.isPartiallySized());
         assertFalse(targetDirSpy.isSized());
 
         for (int i = 1; i <= 2; i++) {
             targetDirSpy.computeSize(listener);
 
             assertTrue(targetDirSpy.isPartiallyExpanded());
             assertTrue(targetDirSpy.isPartiallySized());
             assertEquals(isFullExpanded, targetDirSpy.isSized());
             assertEquals(11L, targetDirSpy.getSize());
             assertEquals(5, targetDirSpy.getTotalTargets());
             assertEquals(3, targetDirSpy.getTotalFiles());
         }
         verify(listener).updateTargetAction(targetDirSpy, SweeperTargetAction.COMPUTE_SIZE);
     }
 
     @Test
     public void testComputeSizeDirectory() throws Exception {
         computeSizeDirectory(false);
         computeSizeDirectory(true);
     }
 
     @Test
     public void testComputeSizeException() throws Exception {
         try {
             target1.computeSize(null);
             fail();
         } catch (NullPointerException e) {
             // expected
         }
 
         try {
             targetDir.computeSize(listener);
             fail();
         } catch (IllegalStateException e) {
             // expected
         }
 
         try {
             prepareDirToSize(targetDir, false, target1).computeSize(listener);
             fail();
         } catch (IllegalStateException e) {
             // expected
             assertFalse(targetDir.isPartiallySized());
             assertFalse(targetDir.isSized());
         }
 
         when(resource1.getSize()).thenThrow(new RuntimeException());
         target1.computeSize(listener);
         assertTrue(target1.isPartiallySized());
         assertFalse(target1.isSized());
         verify(listener).updateTargetException(eq(target1), eq(SweeperTargetAction.COMPUTE_SIZE), any(SweeperException.class));
     }
 
     @Test
     public void testComputeHashFile() throws Exception {
         when(resource1.getModificationDate()).thenReturn(new DateTime(100L));
         when(resource1.getInputStream()).thenReturn(new ByteArrayInputStream("foo".getBytes("UTF-8")));
         target1 = spy(target1);
         when(target1.isSized()).thenReturn(true);
        doReturn(20L).when(target1).getSize();
 
         assertFalse(target1.isPartiallyHashed());
         assertFalse(target1.isHashed());
 
         for (int i = 1; i <= 2; i++) {
             target1.computeHash(listener);
 
             assertTrue(target1.isPartiallyHashed());
             assertTrue(target1.isHashed());
             assertEquals(100L, target1.getModificationDate().getMillis());
             assertEquals(20L + "0beec7b5ea3f0fdbc95d0dd47f3c5bc275da8a33", target1.getHash());
         }
         verify(listener).updateTargetAction(target1, SweeperTargetAction.COMPUTE_HASH);
     }
 
     private SweeperTargetImpl prepareChildToHash(SweeperTargetImpl target, long lastModifiedMillis, long size, String hash) {
         target = spy(target);
         when(target.isPartiallyHashed()).thenReturn(true);
         when(target.isHashed()).thenReturn(true);
         when(target.getModificationDate()).thenReturn(new DateTime(lastModifiedMillis));
         when(target.getHash()).thenReturn(hash);
         doReturn(size).when(target).getSize();
         return target;
     }
 
     private SweeperTargetImpl prepareDirToHash(SweeperTargetImpl target, long size, SweeperTargetImpl... children) {
         target = spy(target);
         when(target.isSized()).thenReturn(true);
        doReturn(size).when(target).getSize();
         when(target.getChildren()).thenReturn(ImmutableList.copyOf(children));
         return target;
     }
 
     private void verifyComputeHashDirectory(long expectedLastModified, String expectedHash, long dirSize, SweeperTargetImpl target, SweeperTargetImpl... children) throws Exception {
         target = prepareDirToHash(target, dirSize, children);
 
         assertFalse(target.isPartiallyHashed());
         assertFalse(target.isHashed());
 
         for (int i = 1; i <= 2; i++) {
             target.computeHash(listener);
 
             assertTrue(target.isPartiallyHashed());
             assertTrue(target.isHashed());
             assertEquals(expectedLastModified, target.getModificationDate().getMillis());
             assertEquals(expectedHash, target.getHash());
         }
         verify(listener).updateTargetAction(target, SweeperTargetAction.COMPUTE_HASH);
     }
 
     @Test
     public void testComputeHashDirectory() throws Exception {
         target1 = prepareChildToHash(target1, 100L, 150L, "foo");
         target2 = prepareChildToHash(target2, 200L, 250L, "bar");
         verifyComputeHashDirectory(200L, 400L + "889ed5b4ac3cb37eb2a39531fb640e7d4d74c2c0", 400L, targetDir, target1, target2);
 
         when(target2.getSize()).thenReturn(0L);
         verifyComputeHashDirectory(100L, "foo", 150L, new SweeperTargetImpl(resourceDir, mockedParent), target1, target2);
     }
 
     @Test
     public void testComputeHashException() throws Exception {
         try {
             target1.computeHash(null);
             fail();
         } catch (NullPointerException e) {
             // expected
         }
 
         try {
             target1.computeHash(listener);
             fail();
         } catch (IllegalStateException e) {
             // expected
         }
 
         try {
             targetDir = prepareDirToHash(targetDir, 0L, target1);
             targetDir.computeHash(listener);
             fail();
         } catch (IllegalStateException e) {
             // expected
             assertFalse(targetDir.isPartiallyHashed());
             assertFalse(targetDir.isHashed());
         }
 
         target1 = prepareChildToHash(target1, 0L, 0L, "");
         when(target1.isHashed()).thenReturn(false);
         targetDir = prepareDirToHash(new SweeperTargetImpl(resourceDir, mockedParent), 0L, target1);
         targetDir.computeHash(listener);
         verify(listener).updateTargetException(eq(targetDir), eq(SweeperTargetAction.COMPUTE_HASH), any(SweeperException.class));
     }
 
     @Test
     public void testHashCode() {
         verifyHashCode(target1, target1Copy);
     }
 
     @Test
     public void testEquals() {
         verifyEquals(target1, target1Copy, target2);
     }
 
     @Test
     public void testToString() {
         verifyToString(target1);
     }
 
     @Test
     public void testCompareTo() throws IOException {
         verifyCompareTo(target1, target1Copy, target2);
     }
 
     @Test(expected = IllegalStateException.class)
     public void testGetSizeException() {
         target1.getSize();
     }
 
     @Test(expected = IllegalStateException.class)
     public void testGetTotalTargetsException() {
         target1.getTotalTargets();
     }
 
     @Test(expected = IllegalStateException.class)
     public void testGetTotalFilesException() {
         target1.getTotalFiles();
     }
 
     @Test(expected = IllegalStateException.class)
     public void testGetHashException() {
         target1.getHash();
     }
 
     @Test(expected = IllegalStateException.class)
     public void testGetModificationDateException() throws Exception {
         target1.getModificationDate();
     }
 
     @Test
     public void testSetMark() {
         assertEquals(Mark.DECIDE_LATER, target1.getMark());
         target1.setPoll(true);
         DuplicateTargetGroup duplicateTargetGroup = mock(DuplicateTargetGroup.class);
         target1.setDuplicateTargetGroup(duplicateTargetGroup);
 
         target1.setMark(Mark.DELETE);
 
         assertEquals(Mark.DELETE, target1.getMark());
         verify(duplicateTargetGroup).setTargetMarked(true);
 
         try {
             target1.setMark(null);
             fail();
         } catch (NullPointerException e) {
             // expected
         }
 
         try {
             target2.setMark(Mark.DELETE);
             fail();
         } catch (IllegalStateException e) {
             // expected
         }
     }
 
     @Test(expected = NullPointerException.class)
     public void testSetDuplicateTargetGroupException() {
         target1.setDuplicateTargetGroup(null);
     }
 
 }
